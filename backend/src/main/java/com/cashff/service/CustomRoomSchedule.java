package com.cashff.service;

import com.cashff.entity.CustomRoom;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomRoomSchedule {
    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a 'IST'", Locale.ENGLISH);
    private final Clock clock;

    public CustomRoomSchedule(Clock clock) {
        this.clock = clock;
    }

    public CustomRoom create(String roomCode, String passwordCode, String startValue, String endValue) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new IllegalArgumentException("Room ID is required.");
        }
        Instant now = clock.instant();
        Instant[] schedule = validateSchedule(startValue, endValue, now);
        Instant startAt = schedule[0];
        Instant endAt = schedule[1];
        return new CustomRoom(roomCode.trim(), passwordCode == null || passwordCode.isBlank()
                ? "111" : passwordCode.trim(), startAt, endAt, now);
    }

    public CustomRoom scheduleLegacy(CustomRoom room, String startValue, String endValue) {
        if (!"Legacy".equals(status(room, clock.instant()))) {
            throw new IllegalArgumentException("Only legacy rooms without a saved schedule can be scheduled.");
        }
        Instant[] schedule = validateSchedule(startValue, endValue, clock.instant());
        room.setStartAt(schedule[0]);
        room.setEndAt(schedule[1]);
        room.setUnlockAt(schedule[0].plusSeconds(180));
        return room;
    }

    private Instant[] validateSchedule(String startValue, String endValue, Instant now) {
        Instant startAt = parseLocalDateTime(startValue, "Start date and time");
        Instant endAt = parseLocalDateTime(endValue, "End date and time");
        if (startAt.isBefore(now)) {
            throw new IllegalArgumentException("Start date and time cannot be in the past (IST).");
        }
        if (!endAt.isAfter(startAt.plusSeconds(180))) {
            throw new IllegalArgumentException("End date and time must be later than the start time plus three minutes.");
        }
        return new Instant[]{startAt, endAt};
    }

    public String status(CustomRoom room, Instant now) {
        if (room.getStartAt() == null || room.getEndAt() == null || room.getUnlockAt() == null) {
            return "Legacy";
        }
        if (!now.isBefore(room.getEndAt())) {
            return "Expired";
        }
        if (now.isBefore(room.getStartAt())) {
            return "Scheduled";
        }
        if (now.isBefore(room.getUnlockAt())) {
            return "Starting";
        }
        return "Active";
    }

    public Map<String, Object> toAdminResponse(CustomRoom room) {
        Instant now = clock.instant();
        String status = status(room, now);
        Map<String, Object> response = baseResponse(room, status, now);
        response.put("roomCode", room.getRoomCode());
        response.put("passwordCode", room.getPasswordCode());
        response.put("canCopy", "Active".equals(status));
        return response;
    }

    public Map<String, Object> toPublicResponse(CustomRoom room) {
        Instant now = clock.instant();
        String status = status(room, now);
        Map<String, Object> response = baseResponse(room, status, now);
        boolean canCopy = "Active".equals(status);
        response.put("roomCode", canCopy ? room.getRoomCode() : null);
        response.put("passwordCode", canCopy ? room.getPasswordCode() : null);
        response.put("canCopy", canCopy);
        return response;
    }

    public long secondsRemaining(CustomRoom room, Instant now) {
        String status = status(room, now);
        Instant target = switch (status) {
            case "Scheduled" -> room.getStartAt();
            case "Starting" -> room.getUnlockAt();
            default -> null;
        };
        return target == null ? 0 : roundedUpSeconds(now, target);
    }

    private long secondsUntilEnd(CustomRoom room, Instant now) {
        if (room.getEndAt() == null || !now.isBefore(room.getEndAt())) {
            return 0;
        }
        return roundedUpSeconds(now, room.getEndAt());
    }

    private long millisecondsUntilTransition(CustomRoom room, String status, Instant now) {
        Instant target = switch (status) {
            case "Scheduled" -> room.getStartAt();
            case "Starting" -> room.getUnlockAt();
            case "Active" -> room.getEndAt();
            default -> null;
        };
        return target == null ? 0 : Math.max(0, Duration.between(now, target).toMillis());
    }

    private long roundedUpSeconds(Instant now, Instant target) {
        long milliseconds = Math.max(0, Duration.between(now, target).toMillis());
        return (milliseconds + 999) / 1000;
    }

    private Instant parseLocalDateTime(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return LocalDateTime.parse(value.trim(), LOCAL_DATE_TIME).atZone(IST).toInstant();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid " + label.toLowerCase() + " value.", exception);
        }
    }

    private Map<String, Object> baseResponse(CustomRoom room, String status, Instant now) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", room.getId());
        response.put("status", status);
        response.put("active", "Active".equals(status));
        response.put("secondsRemaining", secondsRemaining(room, now));
        response.put("secondsUntilEnd", "Active".equals(status) ? secondsUntilEnd(room, now) : 0);
        response.put("millisecondsUntilTransition", millisecondsUntilTransition(room, status, now));
        response.put("startAt", room.getStartAt() == null ? null : room.getStartAt().toString());
        response.put("endAt", room.getEndAt() == null ? null : room.getEndAt().toString());
        response.put("displayStart", formatIst(room.getStartAt()));
        response.put("displayEnd", formatIst(room.getEndAt()));
        response.put("unlockAt", room.getUnlockAt() == null ? null : room.getUnlockAt().toString());
        response.put("publishedAt", room.getPublishedAt() == null ? null : room.getPublishedAt().toString());
        response.put("serverNow", now.toString());
        response.put("photos", photos(room));
        return response;
    }

    private String formatIst(Instant value) {
        return value == null ? null : value.atZone(IST).format(DISPLAY_DATE_TIME);
    }

    private List<String> photos(CustomRoom room) {
        List<String> photos = new ArrayList<>();
        addPhoto(photos, room.getFirstPhoto());
        addPhoto(photos, room.getSecondPhoto());
        addPhoto(photos, room.getThirdPhoto());
        return photos;
    }

    private void addPhoto(List<String> photos, String value) {
        if (value != null && !value.isBlank()) {
            photos.add(value);
        }
    }
}

package com.cashff.service;

import com.cashff.entity.CustomRoom;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomRoomScheduleTest {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final Instant START = Instant.parse("2026-10-14T11:30:00Z");
    private static final Instant END = START.plusSeconds(4 * 60 * 60);

    @Test
    void statusChangesAtSavedScheduleBoundaries() {
        CustomRoom room = room(START, END);
        CustomRoomSchedule beforeStart = scheduleAt(START.minusSeconds(1));
        assertEquals("Scheduled", beforeStart.status(room, START.minusSeconds(1)));
        assertNull(beforeStart.toPublicResponse(room).get("roomCode"));
        assertNull(beforeStart.toPublicResponse(room).get("passwordCode"));

        CustomRoomSchedule atStart = scheduleAt(START);
        assertEquals("Starting", atStart.status(room, START));
        assertEquals(180L, atStart.secondsRemaining(room, START));
        assertFalse((Boolean) atStart.toPublicResponse(room).get("canCopy"));

        Instant beforeUnlock = START.plusSeconds(179);
        assertEquals("Starting", scheduleAt(beforeUnlock).status(room, beforeUnlock));
        assertNull(scheduleAt(beforeUnlock).toPublicResponse(room).get("roomCode"));

        Instant unlocked = START.plusSeconds(180);
        CustomRoomSchedule active = scheduleAt(unlocked);
        assertEquals("Active", active.status(room, unlocked));
        assertEquals("12457", active.toPublicResponse(room).get("roomCode"));
        assertEquals("111", active.toPublicResponse(room).get("passwordCode"));
        assertTrue((Boolean) active.toPublicResponse(room).get("canCopy"));

        CustomRoomSchedule expired = scheduleAt(END);
        assertEquals("Expired", expired.status(room, END));
        assertNull(expired.toPublicResponse(room).get("roomCode"));
        assertNull(expired.toPublicResponse(room).get("passwordCode"));
        assertFalse((Boolean) expired.toPublicResponse(room).get("canCopy"));
    }

    @Test
    void countdownUsesPersistedUnlockAtAfterServiceRecreation() {
        CustomRoom saved = room(START, END);
        CustomRoomSchedule afterRefresh = scheduleAt(START.plusSeconds(75));

        assertEquals(105L, afterRefresh.secondsRemaining(saved, START.plusSeconds(75)));
        assertEquals("Starting", afterRefresh.status(saved, START.plusSeconds(75)));
    }

    @Test
    void unscheduledLegacyRoomHasNoAutomaticTimer() {
        CustomRoom legacy = new CustomRoom("12457", "111", "7:00 PM");
        CustomRoomSchedule schedule = scheduleAt(START);

        assertEquals("Legacy", schedule.status(legacy, START));
        assertEquals(0L, schedule.secondsRemaining(legacy, START));
        assertNull(schedule.toPublicResponse(legacy).get("roomCode"));
        assertNull(schedule.toPublicResponse(legacy).get("passwordCode"));
    }

    @Test
    void startAndEndConvertFromExplicitIstAndSupportMidnight() {
        Instant start = LocalDateTime.parse("2026-10-14T23:58").atZone(IST).toInstant();
        Instant end = LocalDateTime.parse("2026-10-15T00:10").atZone(IST).toInstant();
        CustomRoomSchedule schedule = scheduleAt(start.minusSeconds(1));
        CustomRoom room = schedule.create("12457", "111", "2026-10-14T23:58", "2026-10-15T00:10");

        assertEquals(start, room.getStartAt());
        assertEquals(end, room.getEndAt());
        assertEquals("Scheduled", schedule.status(room, start.minusSeconds(1)));
        assertEquals("Active", schedule.status(room, start.plusSeconds(180)));
        assertEquals("Expired", schedule.status(room, end));
        assertEquals("15 October 2026, 12:10 AM IST", schedule.toAdminResponse(room).get("displayEnd"));
    }

    @Test
    void invalidOrPastSchedulesAreRejected() {
        CustomRoomSchedule schedule = scheduleAt(START);

        assertThrows(IllegalArgumentException.class,
                () -> schedule.create("12457", "111", "not-a-time", "2026-10-14T12:00"));
        assertThrows(IllegalArgumentException.class,
                () -> schedule.create("12457", "111", "2026-10-14T17:29", "2026-10-14T17:32"));
        assertThrows(IllegalArgumentException.class,
                () -> schedule.create("12457", "111", "2026-10-14T17:30", "2026-10-14T17:33"));
        assertThrows(IllegalArgumentException.class,
                () -> schedule.create("12457", "111", "2026-10-14T16:59", "2026-10-14T18:00"));
    }

    @Test
    void adminsCanManageScheduledRoomsWithoutExposingCredentialsPublicly() {
        CustomRoom room = room(START, END);
        CustomRoomSchedule schedule = scheduleAt(START.minusSeconds(1));
        Map<String, Object> adminResponse = schedule.toAdminResponse(room);
        Map<String, Object> publicResponse = schedule.toPublicResponse(room);

        assertEquals("12457", adminResponse.get("roomCode"));
        assertEquals("111", adminResponse.get("passwordCode"));
        assertEquals("Scheduled", adminResponse.get("status"));
        assertTrue(adminResponse.containsKey("photos"));
        assertNull(publicResponse.get("roomCode"));
        assertNull(publicResponse.get("passwordCode"));
        assertFalse((Boolean) publicResponse.get("canCopy"));
    }

    private CustomRoom room(Instant startAt, Instant endAt) {
        CustomRoom room = new CustomRoom("12457", "111", startAt, endAt, START.minusSeconds(3600));
        room.setId(12L);
        return room;
    }

    private CustomRoomSchedule scheduleAt(Instant instant) {
        return new CustomRoomSchedule(Clock.fixed(instant, IST));
    }
}

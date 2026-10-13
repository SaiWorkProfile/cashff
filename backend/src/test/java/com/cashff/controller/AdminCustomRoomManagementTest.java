package com.cashff.controller;

import com.cashff.dto.RoomScheduleInput;
import com.cashff.entity.CustomRoom;
import com.cashff.repository.AppSettingRepository;
import com.cashff.repository.CustomRoomRepository;
import com.cashff.repository.DonationRepository;
import com.cashff.repository.DonorSlotRepository;
import com.cashff.repository.RedeemCodeRepository;
import com.cashff.service.CustomRoomSchedule;
import com.cashff.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCustomRoomManagementTest {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Test
    void adminCanReviewScheduledRoomsUploadWinnerPhotosAndDeleteRoom() {
        CustomRoomRepository rooms = mock(CustomRoomRepository.class);
        FileStorageService storage = mock(FileStorageService.class);
        Instant now = Instant.parse("2026-10-14T11:30:00Z");
        CustomRoom room = new CustomRoom("12457", "111", now.plusSeconds(60), now.plusSeconds(3600), now);
        room.setId(99L);
        CustomRoomSchedule schedule = new CustomRoomSchedule(Clock.fixed(now, IST));
        AdminController controller = new AdminController(mock(DonorSlotRepository.class),
                mock(RedeemCodeRepository.class), rooms, mock(AppSettingRepository.class),
                mock(DonationRepository.class), storage, schedule);
        when(rooms.findAllByOrderByPublishedAtDesc()).thenReturn(List.of(room));

        Map<String, Object> listedRoom = controller.allRooms().get(0);
        assertEquals("Scheduled", listedRoom.get("status"));
        assertEquals("12457", listedRoom.get("roomCode"));
        assertEquals("111", listedRoom.get("passwordCode"));

        when(rooms.findById(99L)).thenReturn(Optional.of(room));
        when(rooms.save(room)).thenReturn(room);
        AtomicInteger photoNumber = new AtomicInteger();
        when(storage.storeImage(any(MultipartFile.class)))
                .thenAnswer(invocation -> "winner-" + photoNumber.incrementAndGet() + ".png");
        controller.uploadRoomPhotos(99L, mock(MultipartFile.class),
                mock(MultipartFile.class), mock(MultipartFile.class));
        assertEquals(List.of("winner-1.png", "winner-2.png", "winner-3.png"),
                schedule.toAdminResponse(room).get("photos"));

        controller.deleteRoom(99L);
        verify(rooms).deleteById(99L);
    }

    @Test
    void adminCanAssignAnIstScheduleToAnExistingLegacyRoom() {
        CustomRoomRepository rooms = mock(CustomRoomRepository.class);
        Instant now = Instant.parse("2026-10-14T11:30:00Z");
        CustomRoom legacy = new CustomRoom("12457", "111", "7:00 PM");
        legacy.setId(100L);
        CustomRoomSchedule schedule = new CustomRoomSchedule(Clock.fixed(now, IST));
        AdminController controller = new AdminController(mock(DonorSlotRepository.class),
                mock(RedeemCodeRepository.class), rooms, mock(AppSettingRepository.class),
                mock(DonationRepository.class), mock(FileStorageService.class), schedule);
        when(rooms.findById(100L)).thenReturn(Optional.of(legacy));
        when(rooms.save(legacy)).thenReturn(legacy);

        Map<String, Object> response = controller.scheduleLegacyRoom(100L,
                new RoomScheduleInput("2026-10-14T18:00", "2026-10-14T21:00"));

        assertEquals("Scheduled", response.get("status"));
        assertEquals("2026-10-14T12:30:00Z", response.get("startAt"));
        assertEquals(legacy.getStartAt().plusSeconds(180), legacy.getUnlockAt());
        assertEquals("12457", response.get("roomCode"));
    }
}

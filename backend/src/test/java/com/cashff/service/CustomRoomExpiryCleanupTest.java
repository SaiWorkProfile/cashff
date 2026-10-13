package com.cashff.service;

import com.cashff.repository.CustomRoomRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CustomRoomExpiryCleanupTest {
    @Test
    void deletesRoomsWhoseEndTimeHasArrived() {
        Instant now = Instant.parse("2026-10-13T10:00:00Z");
        CustomRoomRepository rooms = mock(CustomRoomRepository.class);
        CustomRoomExpiryCleanup cleanup = new CustomRoomExpiryCleanup(
                rooms, Clock.fixed(now, ZoneId.of("Asia/Kolkata")));

        cleanup.deleteExpiredRooms();

        verify(rooms).deleteByEndAtLessThanEqual(now);
    }
}

package com.cashff.service;

import com.cashff.repository.CustomRoomRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Component
public class CustomRoomExpiryCleanup {
    private final CustomRoomRepository rooms;
    private final Clock clock;

    public CustomRoomExpiryCleanup(CustomRoomRepository rooms, Clock clock) {
        this.rooms = rooms;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.custom-rooms.cleanup-interval-ms:1000}")
    @Transactional
    public void deleteExpiredRooms() {
        rooms.deleteByEndAtLessThanEqual(clock.instant());
    }
}

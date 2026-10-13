package com.cashff.dto;

import jakarta.validation.constraints.NotBlank;

public record RoomInput(
        @NotBlank(message = "Room ID is required.") String roomCode,
        @NotBlank(message = "Start date and time is required.") String startAt,
        @NotBlank(message = "End date and time is required.") String endAt,
        String passwordCode) {
}

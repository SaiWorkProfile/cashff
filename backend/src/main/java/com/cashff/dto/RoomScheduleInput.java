package com.cashff.dto;

import jakarta.validation.constraints.NotBlank;

public record RoomScheduleInput(
        @NotBlank(message = "Start date and time is required.") String startAt,
        @NotBlank(message = "End date and time is required.") String endAt) {
}

package com.cashff.dto; import jakarta.validation.constraints.NotBlank; public record RoomInput(@NotBlank String roomCode,@NotBlank String slotTime,String passwordCode){}

package com.cashff.dto; import jakarta.validation.constraints.NotBlank; public record BulkCodesInput(@NotBlank String codes, String startAt, String endAt){}

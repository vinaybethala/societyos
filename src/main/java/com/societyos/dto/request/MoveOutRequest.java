package com.societyos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Request to record resident move-out")
public record MoveOutRequest(
        @Schema(example = "2026-11-30")
        @NotNull(message = "Move-out date is required")
        LocalDate moveOutDate
) {
}

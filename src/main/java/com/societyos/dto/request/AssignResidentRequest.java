package com.societyos.dto.request;

import com.societyos.entity.enums.ResidentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Request to assign a registered user to a flat as resident")
public record AssignResidentRequest(
        @Schema(example = "2")
        @NotNull(message = "User ID is required")
        Long userId,

        @Schema(example = "5")
        @NotNull(message = "Flat ID is required")
        Long flatId,

        @Schema(example = "OWNER")
        @NotNull(message = "Resident type is required (OWNER or TENANT)")
        ResidentType residentType,

        @Schema(example = "2026-11-01")
        @NotNull(message = "Move-in date is required")
        LocalDate moveInDate,

        @Schema(example = "true")
        boolean isPrimaryContact
) {
}

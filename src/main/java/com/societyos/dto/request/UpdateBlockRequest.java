package com.societyos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an apartment block / tower")
public record UpdateBlockRequest(
        @Schema(example = "Tower A - Phase 1")
        @NotBlank(message = "Block name is required")
        @Size(min = 1, max = 50, message = "Block name must be between 1 and 50 characters")
        String name,

        @Schema(example = "Updated East Wing Residential Tower")
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @Schema(example = "18")
        @Min(value = 1, message = "Total floors must be at least 1")
        int totalFloors
) {
}


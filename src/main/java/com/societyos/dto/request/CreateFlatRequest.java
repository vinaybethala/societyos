package com.societyos.dto.request;

import com.societyos.entity.enums.FlatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to register a new flat unit within a block")
public record CreateFlatRequest(
        @Schema(example = "1")
        @NotNull(message = "Block ID is required")
        Long blockId,

        @Schema(example = "101")
        @NotBlank(message = "Flat number is required")
        @Size(max = 20, message = "Flat number cannot exceed 20 characters")
        String flatNumber,

        @Schema(example = "1")
        @Min(value = 0, message = "Floor number cannot be negative")
        int floorNumber,

        @Schema(example = "TWO_BHK")
        @NotNull(message = "Flat type is required")
        FlatType flatType
) {
}

package com.societyos.dto.request;

import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update flat details")
public record UpdateFlatRequest(
        @Schema(example = "2")
        @Min(value = 0, message = "Floor number cannot be negative")
        int floorNumber,

        @Schema(example = "THREE_BHK")
        @NotNull(message = "Flat type is required")
        FlatType flatType,

        @Schema(example = "OCCUPIED")
        @NotNull(message = "Flat status is required")
        FlatStatus status
) {
}


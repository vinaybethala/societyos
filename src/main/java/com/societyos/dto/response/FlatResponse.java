package com.societyos.dto.response;

import com.societyos.entity.Flat;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Flat unit representation")
public record FlatResponse(
        Long id,
        Long blockId,
        String blockName,
        String flatNumber,
        int floorNumber,
        FlatType flatType,
        FlatStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FlatResponse fromEntity(Flat flat) {
        return new FlatResponse(
                flat.getId(),
                flat.getBlock() != null ? flat.getBlock().getId() : null,
                flat.getBlock() != null ? flat.getBlock().getName() : null,
                flat.getFlatNumber(),
                flat.getFloorNumber(),
                flat.getFlatType(),
                flat.getStatus(),
                flat.getCreatedAt(),
                flat.getUpdatedAt()
        );
    }
}


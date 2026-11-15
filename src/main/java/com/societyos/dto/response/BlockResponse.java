package com.societyos.dto.response;

import com.societyos.entity.Block;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Block / Tower details")
public record BlockResponse(
        Long id,
        String name,
        String description,
        int totalFloors,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BlockResponse fromEntity(Block block) {
        return new BlockResponse(
                block.getId(),
                block.getName(),
                block.getDescription(),
                block.getTotalFloors(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}

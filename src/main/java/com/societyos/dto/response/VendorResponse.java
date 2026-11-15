package com.societyos.dto.response;

import com.societyos.entity.Vendor;
import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Vendor representation")
public record VendorResponse(
        Long id,
        String name,
        VendorServiceType serviceType,
        String phone,
        String email,
        VendorStatus status,
        BigDecimal averageRating,
        int totalJobsCompleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static VendorResponse fromEntity(Vendor vendor) {
        return new VendorResponse(
                vendor.getId(),
                vendor.getName(),
                vendor.getServiceType(),
                vendor.getPhone(),
                vendor.getEmail(),
                vendor.getStatus(),
                vendor.getAverageRating(),
                vendor.getTotalJobsCompleted(),
                vendor.getCreatedAt(),
                vendor.getUpdatedAt()
        );
    }
}

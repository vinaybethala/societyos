package com.societyos.dto.response;

import com.societyos.entity.Resident;
import com.societyos.entity.enums.ResidentStatus;
import com.societyos.entity.enums.ResidentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Resident occupancy details")
public record ResidentResponse(
        Long id,
        Long userId,
        String username,
        String userFullName,
        String userEmail,
        String userPhone,
        Long flatId,
        String flatNumber,
        String blockName,
        ResidentType residentType,
        LocalDate moveInDate,
        LocalDate moveOutDate,
        boolean isPrimaryContact,
        ResidentStatus status,
        LocalDateTime createdAt
) {
    public static ResidentResponse fromEntity(Resident resident) {
        return new ResidentResponse(
                resident.getId(),
                resident.getUser() != null ? resident.getUser().getId() : null,
                resident.getUser() != null ? resident.getUser().getUsername() : null,
                resident.getUser() != null ? resident.getUser().getFullName() : null,
                resident.getUser() != null ? resident.getUser().getEmail() : null,
                resident.getUser() != null ? resident.getUser().getPhone() : null,
                resident.getFlat() != null ? resident.getFlat().getId() : null,
                resident.getFlat() != null ? resident.getFlat().getFlatNumber() : null,
                resident.getFlat() != null && resident.getFlat().getBlock() != null
                        ? resident.getFlat().getBlock().getName() : null,
                resident.getResidentType(),
                resident.getMoveInDate(),
                resident.getMoveOutDate(),
                resident.isPrimaryContact(),
                resident.getStatus(),
                resident.getCreatedAt()
        );
    }
}


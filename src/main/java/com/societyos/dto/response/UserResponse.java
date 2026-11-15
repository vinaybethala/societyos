package com.societyos.dto.response;

import com.societyos.entity.User;
import com.societyos.entity.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "Safe user profile representation")
public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        UserStatus status,
        Set<String> roles,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet())
                : Set.of();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getStatus(),
                roleNames,
                user.getCreatedAt()
        );
    }
}


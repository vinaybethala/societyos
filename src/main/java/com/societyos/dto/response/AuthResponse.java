package com.societyos.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT access token")
public record AuthResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(example = "86400000")
        long expiresInMs,

        UserResponse user
) {
    public static AuthResponse of(String token, long expiresInMs, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresInMs, user);
    }
}


package com.societyos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login credentials")
public record LoginRequest(
        @Schema(example = "admin@societyos.com", description = "Username or Email address")
        @NotBlank(message = "Username or Email is required")
        String usernameOrEmail,

        @Schema(example = "Admin@123", description = "Account password")
        @NotBlank(message = "Password is required")
        String password
) {
}


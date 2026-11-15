package com.societyos.dto.request;

import com.societyos.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration payload")
public record RegisterRequest(
        @Schema(example = "john_doe")
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Schema(example = "john@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,

        @Schema(example = "SecureP@ss123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        String password,

        @Schema(example = "John Doe")
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(example = "+919876543210")
        @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "Phone must be a valid phone number with optional + prefix and 7-15 digits")
        String phone,

        @Schema(example = "ROLE_RESIDENT", description = "Role requested (ROLE_RESIDENT, ROLE_SECURITY, ROLE_ADMIN). Defaults to ROLE_RESIDENT if omitted.")
        RoleType role
) {
}


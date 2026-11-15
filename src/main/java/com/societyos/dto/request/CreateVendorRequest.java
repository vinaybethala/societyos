package com.societyos.dto.request;

import com.societyos.entity.enums.VendorServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to register a new vendor/staff")
public record CreateVendorRequest(
        @Schema(example = "Ramesh Plumbing Works")
        @NotBlank(message = "Vendor name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @Schema(example = "PLUMBER")
        @NotNull(message = "Service type is required")
        VendorServiceType serviceType,

        @Schema(example = "+919876543222")
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must be a valid format with optional + prefix and 7-15 digits")
        String phone,

        @Schema(example = "ramesh.plumber@example.com")
        @Email(message = "Email must be a valid email format")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email
) {
}


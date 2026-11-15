package com.societyos.dto.request;

import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update vendor details")
public record UpdateVendorRequest(
        @Schema(example = "Ramesh Plumbing Works & Co.")
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

        @Schema(example = "ramesh.new@example.com")
        @Email(message = "Email must be a valid email format")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,

        @Schema(example = "ACTIVE")
        @NotNull(message = "Status is required")
        VendorStatus status
) {
}

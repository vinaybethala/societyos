package com.societyos.controller;

import com.societyos.dto.request.CreateVendorRequest;
import com.societyos.dto.request.UpdateVendorRequest;
import com.societyos.dto.response.ErrorResponse;
import com.societyos.dto.response.VendorResponse;
import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
import com.societyos.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors")
@Tag(name = "Vendor Management", description = "Endpoints for managing service contractors and staff (plumbers, electricians, etc.)")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new vendor", description = "Admin only. Registers a service contractor.")
    @ApiResponse(responseCode = "201", description = "Vendor created",
            content = @Content(schema = @Schema(implementation = VendorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Phone number already registered for another vendor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<VendorResponse> createVendor(@Valid @RequestBody CreateVendorRequest request) {
        VendorResponse response = vendorService.createVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update vendor details", description = "Admin only. Updates vendor info or status.")
    @ApiResponse(responseCode = "200", description = "Vendor updated",
            content = @Content(schema = @Schema(implementation = VendorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Vendor not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<VendorResponse> updateVendor(@PathVariable Long id, @Valid @RequestBody UpdateVendorRequest request) {
        VendorResponse response = vendorService.updateVendor(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT')")
    @Operation(summary = "List vendors", description = "Returns paginated list of vendors with optional filtering by service type and status.")
    @ApiResponse(responseCode = "200", description = "Paginated vendors returned")
    public ResponseEntity<Page<VendorResponse>> getAllVendors(
            @RequestParam(required = false) VendorServiceType serviceType,
            @RequestParam(required = false) VendorStatus status,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<VendorResponse> vendors = vendorService.getAllVendors(serviceType, status, pageable);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT')")
    @Operation(summary = "Get vendor by ID", description = "Returns details for a specific vendor.")
    @ApiResponse(responseCode = "200", description = "Vendor found",
            content = @Content(schema = @Schema(implementation = VendorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Vendor not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable Long id) {
        VendorResponse vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(vendor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate vendor", description = "Admin only. Soft-deletes a vendor by setting status to INACTIVE.")
    @ApiResponse(responseCode = "204", description = "Vendor deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Vendor not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }
}

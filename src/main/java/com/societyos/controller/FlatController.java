package com.societyos.controller;

import com.societyos.dto.request.CreateFlatRequest;
import com.societyos.dto.request.UpdateFlatRequest;
import com.societyos.dto.response.ErrorResponse;
import com.societyos.dto.response.FlatResponse;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import com.societyos.service.FlatService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/flats")
@Tag(name = "Society Flats", description = "Endpoints for managing residential units / flats")
public class FlatController {

    private final FlatService flatService;

    public FlatController(FlatService flatService) {
        this.flatService = flatService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new flat", description = "Admin only. Registers a flat unit within a block.")
    @ApiResponse(responseCode = "201", description = "Flat created",
            content = @Content(schema = @Schema(implementation = FlatResponse.class)))
    @ApiResponse(responseCode = "409", description = "Flat number already exists in block",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FlatResponse> createFlat(@Valid @RequestBody CreateFlatRequest request) {
        FlatResponse response = flatService.createFlat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update flat details", description = "Admin only. Updates flat type, floor, or status.")
    @ApiResponse(responseCode = "200", description = "Flat updated",
            content = @Content(schema = @Schema(implementation = FlatResponse.class)))
    @ApiResponse(responseCode = "404", description = "Flat not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FlatResponse> updateFlat(@PathVariable Long id, @Valid @RequestBody UpdateFlatRequest request) {
        FlatResponse response = flatService.updateFlat(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'SECURITY')")
    @Operation(summary = "List flats", description = "Returns paginated flats with optional filtering by block, type, and status.")
    @ApiResponse(responseCode = "200", description = "Paginated flats returned")
    public ResponseEntity<Page<FlatResponse>> getAllFlats(
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) FlatType flatType,
            @RequestParam(required = false) FlatStatus status,
            @PageableDefault(size = 20, sort = "flatNumber", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<FlatResponse> flats = flatService.getAllFlats(blockId, flatType, status, pageable);
        return ResponseEntity.ok(flats);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'SECURITY')")
    @Operation(summary = "Get flat by ID", description = "Returns details for a specific flat unit.")
    @ApiResponse(responseCode = "200", description = "Flat found",
            content = @Content(schema = @Schema(implementation = FlatResponse.class)))
    @ApiResponse(responseCode = "404", description = "Flat not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FlatResponse> getFlatById(@PathVariable Long id) {
        FlatResponse flat = flatService.getFlatById(id);
        return ResponseEntity.ok(flat);
    }

    @GetMapping("/block/{blockId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'SECURITY')")
    @Operation(summary = "List all flats in a block", description = "Returns all flats located in the specified block.")
    @ApiResponse(responseCode = "200", description = "List of flats returned")
    public ResponseEntity<List<FlatResponse>> getFlatsByBlockId(@PathVariable Long blockId) {
        List<FlatResponse> flats = flatService.getFlatsByBlockId(blockId);
        return ResponseEntity.ok(flats);
    }
}


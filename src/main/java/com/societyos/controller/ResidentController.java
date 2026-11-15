package com.societyos.controller;

import com.societyos.dto.request.AssignResidentRequest;
import com.societyos.dto.request.MoveOutRequest;
import com.societyos.dto.response.ErrorResponse;
import com.societyos.dto.response.ResidentResponse;
import com.societyos.entity.enums.ResidentStatus;
import com.societyos.entity.enums.ResidentType;
import com.societyos.service.ResidentService;
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
@RequestMapping("/api/v1/residents")
@Tag(name = "Resident & Occupancy Management", description = "Endpoints for managing flat occupancy, owner/tenant assignments, and move-outs")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign resident to flat", description = "Admin only. Maps a user to a flat as owner or tenant and updates flat occupancy status to OCCUPIED.")
    @ApiResponse(responseCode = "201", description = "Resident assigned",
            content = @Content(schema = @Schema(implementation = ResidentResponse.class)))
    @ApiResponse(responseCode = "404", description = "User or Flat not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "User is already an active resident of this flat",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ResidentResponse> assignResident(@Valid @RequestBody AssignResidentRequest request) {
        ResidentResponse response = residentService.assignResident(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/move-out")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record resident move-out", description = "Admin only. Marks resident as MOVED_OUT. If no active residents remain, flat is marked VACANT.")
    @ApiResponse(responseCode = "200", description = "Move-out recorded",
            content = @Content(schema = @Schema(implementation = ResidentResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid move-out date or resident already moved out",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Resident record not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ResidentResponse> recordMoveOut(@PathVariable Long id, @Valid @RequestBody MoveOutRequest request) {
        ResidentResponse response = residentService.recordMoveOut(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-flats")
    @PreAuthorize("hasRole('RESIDENT')")
    @Operation(summary = "Get current resident's flats", description = "Returns the flats assigned to the currently authenticated resident.")
    @ApiResponse(responseCode = "200", description = "Active flat assignments returned")
    public ResponseEntity<List<ResidentResponse>> getMyFlats() {
        List<ResidentResponse> flats = residentService.getMyFlats();
        return ResponseEntity.ok(flats);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get resident by ID", description = "Admin only. Returns resident record details.")
    @ApiResponse(responseCode = "200", description = "Resident record found",
            content = @Content(schema = @Schema(implementation = ResidentResponse.class)))
    @ApiResponse(responseCode = "404", description = "Resident record not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ResidentResponse> getResidentById(@PathVariable Long id) {
        ResidentResponse resident = residentService.getResidentById(id);
        return ResponseEntity.ok(resident);
    }

    @GetMapping("/flat/{flatId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY')")
    @Operation(summary = "Get residents of a flat", description = "Returns all residents registered to the given flat.")
    @ApiResponse(responseCode = "200", description = "Residents list returned")
    public ResponseEntity<List<ResidentResponse>> getResidentsByFlat(@PathVariable Long flatId) {
        List<ResidentResponse> residents = residentService.getResidentsByFlat(flatId);
        return ResponseEntity.ok(residents);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all residents", description = "Admin only. Paginated query with filters for flat, type, and status.")
    @ApiResponse(responseCode = "200", description = "Paginated residents returned")
    public ResponseEntity<Page<ResidentResponse>> getAllResidents(
            @RequestParam(required = false) Long flatId,
            @RequestParam(required = false) ResidentType residentType,
            @RequestParam(required = false) ResidentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ResidentResponse> residents = residentService.getAllResidents(flatId, residentType, status, pageable);
        return ResponseEntity.ok(residents);
    }
}

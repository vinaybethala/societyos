package com.societyos.controller;

import com.societyos.dto.request.CreateBlockRequest;
import com.societyos.dto.request.UpdateBlockRequest;
import com.societyos.dto.response.BlockResponse;
import com.societyos.dto.response.ErrorResponse;
import com.societyos.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blocks")
@Tag(name = "Society Blocks / Towers", description = "Endpoints for managing residential towers/wings")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new block", description = "Admin only. Registers a new tower/block in the society.")
    @ApiResponse(responseCode = "201", description = "Block created",
            content = @Content(schema = @Schema(implementation = BlockResponse.class)))
    @ApiResponse(responseCode = "409", description = "Block name already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BlockResponse> createBlock(@Valid @RequestBody CreateBlockRequest request) {
        BlockResponse response = blockService.createBlock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update block details", description = "Admin only. Updates block metadata.")
    @ApiResponse(responseCode = "200", description = "Block updated",
            content = @Content(schema = @Schema(implementation = BlockResponse.class)))
    @ApiResponse(responseCode = "404", description = "Block not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BlockResponse> updateBlock(@PathVariable Long id, @Valid @RequestBody UpdateBlockRequest request) {
        BlockResponse response = blockService.updateBlock(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'SECURITY')")
    @Operation(summary = "Get all blocks", description = "Returns a list of all society blocks.")
    @ApiResponse(responseCode = "200", description = "List of blocks returned")
    public ResponseEntity<List<BlockResponse>> getAllBlocks() {
        List<BlockResponse> blocks = blockService.getAllBlocks();
        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'SECURITY')")
    @Operation(summary = "Get block by ID", description = "Returns details for a specific block.")
    @ApiResponse(responseCode = "200", description = "Block found",
            content = @Content(schema = @Schema(implementation = BlockResponse.class)))
    @ApiResponse(responseCode = "404", description = "Block not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BlockResponse> getBlockById(@PathVariable Long id) {
        BlockResponse block = blockService.getBlockById(id);
        return ResponseEntity.ok(block);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete block", description = "Admin only. Deletes a block if no flats are registered within it.")
    @ApiResponse(responseCode = "204", description = "Block deleted successfully")
    @ApiResponse(responseCode = "409", description = "Cannot delete block containing flats",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteBlock(@PathVariable Long id) {
        blockService.deleteBlock(id);
        return ResponseEntity.noContent().build();
    }
}


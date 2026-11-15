package com.societyos.controller;

import com.societyos.dto.response.ErrorResponse;
import com.societyos.dto.response.UserResponse;
import com.societyos.entity.enums.RoleType;
import com.societyos.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management (Admin)", description = "Administrative endpoints to query and manage registered users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all registered users", description = "Admin only. Returns paginated list of users with optional role filtering.")
    @ApiResponse(responseCode = "200", description = "Paginated users returned")
    @ApiResponse(responseCode = "403", description = "Access denied for non-admin users",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) RoleType role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserResponse> users = userService.getAllUsers(role, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user details by ID", description = "Admin only. Returns safe user profile by ID.")
    @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}

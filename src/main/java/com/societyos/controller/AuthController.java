package com.societyos.controller;

import com.societyos.dto.request.LoginRequest;
import com.societyos.dto.request.RegisterRequest;
import com.societyos.dto.response.AuthResponse;
import com.societyos.dto.response.ErrorResponse;
import com.societyos.dto.response.UserResponse;
import com.societyos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & Onboarding", description = "Endpoints for user registration, authentication, and token verification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account", description = "Creates a new user profile with requested role (default: ROLE_RESIDENT) and returns a signed JWT.")
    @ApiResponse(responseCode = "201", description = "User successfully registered",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation errors on request body",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username or Email already in use",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates credentials and returns a JWT access token.")
    @ApiResponse(responseCode = "200", description = "Authentication successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", description = "Returns identity and profile details of the user extracted from the JWT token in Authorization header.")
    @ApiResponse(responseCode = "200", description = "Profile returned",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}

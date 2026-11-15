package com.societyos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societyos.dto.request.LoginRequest;
import com.societyos.dto.request.RegisterRequest;
import com.societyos.dto.response.AuthResponse;
import com.societyos.entity.Role;
import com.societyos.entity.User;
import com.societyos.entity.enums.RoleType;
import com.societyos.entity.enums.UserStatus;
import com.societyos.repository.RoleRepository;
import com.societyos.repository.UserRepository;
import com.societyos.security.JwtService;
import com.societyos.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Role adminRole;
    private Role residentRole;

    @BeforeEach
    void setUp() {
        adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleType.ROLE_ADMIN).description("Admin").build()));
        residentRole = roleRepository.findByName(RoleType.ROLE_RESIDENT).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleType.ROLE_RESIDENT).description("Resident").build()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should register resident and return 201 Created")
    void shouldRegisterNewResident() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        RegisterRequest request = new RegisterRequest(
                "user_" + uniqueSuffix,
                "user_" + uniqueSuffix + "@societyos.com",
                "Password@123",
                "Integration Resident",
                "+919876543210",
                RoleType.ROLE_RESIDENT
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.username", is(request.username())))
                .andExpect(jsonPath("$.user.email", is(request.email())))
                .andExpect(jsonPath("$.user.roles", hasItem("ROLE_RESIDENT")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with invalid data should return 400 Bad Request with field errors")
    void shouldFailValidationOnBlankFields() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(
                "", // Blank username
                "invalid-email-format",
                "123", // Too short
                "", // Blank full name
                "invalid_phone",
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.email", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.fullName", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with valid credentials should return 200 OK and JWT token")
    void shouldLoginSuccessfully() throws Exception {
        String username = "login_user_" + System.currentTimeMillis();
        String email = username + "@societyos.com";
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode("SecretPass123"))
                .fullName("Login Test User")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(residentRole))
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest(username, "SecretPass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.user.username", is(username)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with invalid password should return 401 Unauthorized")
    void shouldRejectInvalidPassword() throws Exception {
        LoginRequest badRequest = new LoginRequest("admin", "WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", containsString("Invalid username/email or password")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return current user profile when authenticated")
    void shouldReturnProfileForAuthenticatedUser() throws Exception {
        String username = "me_user_" + System.currentTimeMillis();
        User user = User.builder()
                .username(username)
                .email(username + "@societyos.com")
                .passwordHash(passwordEncoder.encode("Pass123"))
                .fullName("Profile User")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(residentRole))
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateToken(UserPrincipal.create(user));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.roles", hasItem("ROLE_RESIDENT")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return 401 Unauthorized when token is missing")
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("RBAC check: Non-admin (RESIDENT) access to GET /api/v1/users must return 403 Forbidden")
    void shouldDenyNonAdminAccessToUsersList() throws Exception {
        String username = "resident_forbidden_" + System.currentTimeMillis();
        User resident = User.builder()
                .username(username)
                .email(username + "@societyos.com")
                .passwordHash(passwordEncoder.encode("Pass123"))
                .fullName("Resident User")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(residentRole))
                .build();
        resident = userRepository.save(resident);

        String residentToken = jwtService.generateToken(UserPrincipal.create(resident));

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("RBAC check: Admin access to GET /api/v1/users must return 200 OK")
    void shouldAllowAdminAccessToUsersList() throws Exception {
        String adminUsername = "admin_allowed_" + System.currentTimeMillis();
        User admin = User.builder()
                .username(adminUsername)
                .email(adminUsername + "@societyos.com")
                .passwordHash(passwordEncoder.encode("AdminPass123"))
                .fullName("Admin Test User")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole))
                .build();
        admin = userRepository.save(admin);

        String adminToken = jwtService.generateToken(UserPrincipal.create(admin));

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }
}


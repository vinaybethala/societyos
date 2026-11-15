package com.societyos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societyos.dto.request.AssignResidentRequest;
import com.societyos.dto.request.CreateBlockRequest;
import com.societyos.dto.request.CreateFlatRequest;
import com.societyos.dto.request.MoveOutRequest;
import com.societyos.dto.response.BlockResponse;
import com.societyos.dto.response.FlatResponse;
import com.societyos.dto.response.ResidentResponse;
import com.societyos.entity.Role;
import com.societyos.entity.User;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import com.societyos.entity.enums.ResidentType;
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

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SocietyStructureIntegrationTest {

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

    private String adminToken;
    private String residentToken;
    private User testResidentUser;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN).orElseGet(() ->
                roleRepository.save(new Role(RoleType.ROLE_ADMIN, "Admin")));
        Role residentRole = roleRepository.findByName(RoleType.ROLE_RESIDENT).orElseGet(() ->
                roleRepository.save(new Role(RoleType.ROLE_RESIDENT, "Resident")));

        long suffix = System.currentTimeMillis();

        User admin = User.builder()
                .username("admin_soc_" + suffix)
                .email("admin_soc_" + suffix + "@societyos.com")
                .passwordHash(passwordEncoder.encode("AdminPass"))
                .fullName("Society Admin")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole))
                .build();
        admin = userRepository.save(admin);
        adminToken = jwtService.generateToken(UserPrincipal.create(admin));

        testResidentUser = User.builder()
                .username("resident_soc_" + suffix)
                .email("resident_soc_" + suffix + "@societyos.com")
                .passwordHash(passwordEncoder.encode("ResPass"))
                .fullName("Society Resident")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(residentRole))
                .build();
        testResidentUser = userRepository.save(testResidentUser);
        residentToken = jwtService.generateToken(UserPrincipal.create(testResidentUser));
    }

    @Test
    @DisplayName("Complete Society Flow: Admin creates block -> creates flat -> assigns resident -> resident checks my-flats -> move out")
    void shouldExecuteCompleteSocietyFlow() throws Exception {
        String blockName = "Tower_" + System.currentTimeMillis();

        // 1. Admin creates Block
        CreateBlockRequest blockRequest = new CreateBlockRequest(blockName, "Premium Tower", 14);
        MvcResult blockResult = mockMvc.perform(post("/api/v1/blocks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(blockName)))
                .andReturn();

        BlockResponse blockResponse = objectMapper.readValue(
                blockResult.getResponse().getContentAsString(), BlockResponse.class);
        Long blockId = blockResponse.id();

        // 2. Admin creates Flat in Block
        CreateFlatRequest flatRequest = new CreateFlatRequest(blockId, "1001", 10, FlatType.THREE_BHK);
        MvcResult flatResult = mockMvc.perform(post("/api/v1/flats")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flatRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flatNumber", is("1001")))
                .andExpect(jsonPath("$.status", is("VACANT")))
                .andReturn();

        FlatResponse flatResponse = objectMapper.readValue(
                flatResult.getResponse().getContentAsString(), FlatResponse.class);
        Long flatId = flatResponse.id();

        // 3. Admin assigns Resident to Flat (Flat status becomes OCCUPIED)
        AssignResidentRequest assignRequest = new AssignResidentRequest(
                testResidentUser.getId(), flatId, ResidentType.TENANT, LocalDate.now(), true);

        MvcResult assignResult = mockMvc.perform(post("/api/v1/residents/assign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(testResidentUser.getId().intValue())))
                .andExpect(jsonPath("$.flatId", is(flatId.intValue())))
                .andReturn();

        ResidentResponse residentResponse = objectMapper.readValue(
                assignResult.getResponse().getContentAsString(), ResidentResponse.class);
        Long residentId = residentResponse.id();

        // Verify flat status is now OCCUPIED
        mockMvc.perform(get("/api/v1/flats/" + flatId)
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OCCUPIED")));

        // 4. Resident retrieves their flat via /my-flats
        mockMvc.perform(get("/api/v1/residents/my-flats")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].flatNumber", is("1001")));

        // 5. Admin records Move-Out for Resident (Flat status reverts to VACANT)
        MoveOutRequest moveOutRequest = new MoveOutRequest(LocalDate.now().plusDays(30));
        mockMvc.perform(patch("/api/v1/residents/" + residentId + "/move-out")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveOutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("MOVED_OUT")));

        // Verify flat status is now VACANT
        mockMvc.perform(get("/api/v1/flats/" + flatId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("VACANT")));
    }

    @Test
    @DisplayName("RBAC check: Non-admin (RESIDENT) attempting to create a block receives 403 Forbidden")
    void shouldDenyBlockCreationForResident() throws Exception {
        CreateBlockRequest blockRequest = new CreateBlockRequest("Unauthorized Tower", "Test", 5);

        mockMvc.perform(post("/api/v1/blocks")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }
}

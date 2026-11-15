package com.societyos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societyos.dto.request.CreateVendorRequest;
import com.societyos.dto.response.VendorResponse;
import com.societyos.entity.Role;
import com.societyos.entity.User;
import com.societyos.entity.enums.RoleType;
import com.societyos.entity.enums.UserStatus;
import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VendorControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN).orElseGet(() ->
                roleRepository.save(new Role(RoleType.ROLE_ADMIN, "Admin")));
        Role residentRole = roleRepository.findByName(RoleType.ROLE_RESIDENT).orElseGet(() ->
                roleRepository.save(new Role(RoleType.ROLE_RESIDENT, "Resident")));

        long suffix = System.currentTimeMillis();

        User admin = User.builder()
                .username("admin_ven_" + suffix)
                .email("admin_ven_" + suffix + "@societyos.com")
                .passwordHash(passwordEncoder.encode("AdminPass"))
                .fullName("Vendor Admin")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole))
                .build();
        admin = userRepository.save(admin);
        adminToken = jwtService.generateToken(UserPrincipal.create(admin));

        User resident = User.builder()
                .username("res_ven_" + suffix)
                .email("res_ven_" + suffix + "@societyos.com")
                .passwordHash(passwordEncoder.encode("ResPass"))
                .fullName("Resident User")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(residentRole))
                .build();
        resident = userRepository.save(resident);
        residentToken = jwtService.generateToken(UserPrincipal.create(resident));
    }

    @Test
    @DisplayName("Admin creates vendor -> Resident views vendor list -> Admin soft-deletes vendor")
    void shouldExecuteVendorLifecycle() throws Exception {
        String phone = "+91" + (System.currentTimeMillis() % 10000000000L);
        CreateVendorRequest createRequest = new CreateVendorRequest(
                "Kumar Electricals", VendorServiceType.ELECTRICIAN, phone, "kumar@example.com");

        // 1. Admin creates Vendor (201 Created)
        MvcResult result = mockMvc.perform(post("/api/v1/vendors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Kumar Electricals")))
                .andExpect(jsonPath("$.serviceType", is("ELECTRICIAN")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn();

        VendorResponse vendorResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), VendorResponse.class);
        Long vendorId = vendorResponse.id();

        // 2. Resident queries vendor list (200 OK)
        mockMvc.perform(get("/api/v1/vendors")
                        .header("Authorization", "Bearer " + residentToken)
                        .param("serviceType", "ELECTRICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));

        // 3. Resident attempts to delete vendor -> 403 Forbidden
        mockMvc.perform(delete("/api/v1/vendors/" + vendorId)
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isForbidden());

        // 4. Admin deletes (soft-deactivates) vendor -> 204 No Content
        mockMvc.perform(delete("/api/v1/vendors/" + vendorId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // 5. Verify vendor status is now INACTIVE
        mockMvc.perform(get("/api/v1/vendors/" + vendorId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INACTIVE")));
    }
}

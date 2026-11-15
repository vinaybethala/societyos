package com.societyos.service;

import com.societyos.config.JwtConfigProperties;
import com.societyos.dto.request.LoginRequest;
import com.societyos.dto.request.RegisterRequest;
import com.societyos.dto.response.AuthResponse;
import com.societyos.entity.Role;
import com.societyos.entity.User;
import com.societyos.entity.enums.RoleType;
import com.societyos.entity.enums.UserStatus;
import com.societyos.exception.ConflictException;
import com.societyos.repository.RoleRepository;
import com.societyos.repository.UserRepository;
import com.societyos.security.JwtService;
import com.societyos.security.UserPrincipal;
import com.societyos.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private JwtService jwtService;
    private AuthServiceImpl authService;

    private Role residentRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        JwtConfigProperties jwtConfig = new JwtConfigProperties();
        jwtConfig.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        jwtConfig.setExpirationMs(3600000L);
        jwtConfig.setIssuer("SocietyOS-Test");
        jwtService = new JwtService(jwtConfig);

        authService = new AuthServiceImpl(
                userRepository,
                roleRepository,
                passwordEncoder,
                authenticationManager,
                jwtService
        );

        residentRole = new Role(RoleType.ROLE_RESIDENT, "Resident");
        residentRole.setId(2L);

        testUser = new User(
                "testresident",
                "resident@societyos.com",
                "encodedPassword",
                "Test Resident",
                "+919999988888",
                UserStatus.ACTIVE,
                Set.of(residentRole)
        );
        testUser.setId(10L);
    }

    @Test
    @DisplayName("Should successfully register a new resident user")
    void shouldRegisterSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "testresident",
                "resident@societyos.com",
                "Password@123",
                "Test Resident",
                "+919999988888",
                RoleType.ROLE_RESIDENT
        );

        when(userRepository.existsByUsername("testresident")).thenReturn(false);
        when(userRepository.existsByEmail("resident@societyos.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_RESIDENT)).thenReturn(Optional.of(residentRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.user().username()).isEqualTo("testresident");
        assertThat(response.user().email()).isEqualTo("resident@societyos.com");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when username is already taken")
    void shouldThrowConflictWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest(
                "existingUser",
                "new@societyos.com",
                "Password@123",
                "New User",
                null,
                null
        );

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username 'existingUser' is already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ConflictException when email is already registered")
    void shouldThrowConflictWhenEmailExists() {
        RegisterRequest request = new RegisterRequest(
                "freshUser",
                "existing@societyos.com",
                "Password@123",
                "Fresh User",
                null,
                null
        );

        when(userRepository.existsByUsername("freshUser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@societyos.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email 'existing@societyos.com' is already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should authenticate and return token on valid login")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("testresident", "Password@123");
        UserPrincipal principal = UserPrincipal.create(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.user().id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should propagate BadCredentialsException when password is wrong")
    void shouldThrowOnBadCredentials() {
        LoginRequest request = new LoginRequest("testresident", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}

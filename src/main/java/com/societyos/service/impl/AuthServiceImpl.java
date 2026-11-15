package com.societyos.service.impl;

import com.societyos.dto.request.LoginRequest;
import com.societyos.dto.request.RegisterRequest;
import com.societyos.dto.response.AuthResponse;
import com.societyos.dto.response.UserResponse;
import com.societyos.entity.Role;
import com.societyos.entity.User;
import com.societyos.entity.enums.RoleType;
import com.societyos.entity.enums.UserStatus;
import com.societyos.exception.ConflictException;
import com.societyos.exception.ResourceNotFoundException;
import com.societyos.repository.RoleRepository;
import com.societyos.repository.UserRepository;
import com.societyos.security.JwtService;
import com.societyos.security.SecurityUtils;
import com.societyos.security.UserPrincipal;
import com.societyos.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedUsername = request.username().trim();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ConflictException("Username '" + normalizedUsername + "' is already taken");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email '" + normalizedEmail + "' is already registered");
        }

        RoleType roleType = request.role() != null ? request.role() : RoleType.ROLE_RESIDENT;
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleType));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .phone(request.phone())
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user id={} username={} with role={}", savedUser.getId(), savedUser.getUsername(), roleType);

        UserPrincipal principal = UserPrincipal.create(savedUser);
        String token = jwtService.generateToken(principal);

        return AuthResponse.of(token, jwtService.getExpirationMs(), UserResponse.fromEntity(savedUser));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usernameOrEmail().trim(),
                        request.password()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        String token = jwtService.generateToken(principal);
        log.info("User logged in successfully: id={} username={}", user.getId(), user.getUsername());

        return AuthResponse.of(token, jwtService.getExpirationMs(), UserResponse.fromEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        return UserResponse.fromEntity(user);
    }
}

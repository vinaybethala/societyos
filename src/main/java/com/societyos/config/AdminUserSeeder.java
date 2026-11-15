package com.societyos.config;

import com.societyos.entity.Role;
import com.societyos.entity.User;
import com.societyos.entity.enums.RoleType;
import com.societyos.entity.enums.UserStatus;
import com.societyos.repository.RoleRepository;
import com.societyos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class AdminUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${societyos.seed.admin.enabled:true}")
    private boolean seedEnabled;

    @Value("${societyos.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${societyos.seed.admin.email:admin@societyos.com}")
    private String adminEmail;

    @Value("${societyos.seed.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${societyos.seed.admin.full-name:System Administrator}")
    private String adminFullName;

    @Value("${societyos.seed.admin.phone:+919876543210}")
    private String adminPhone;

    public AdminUserSeeder(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Admin user seeding is disabled via configuration.");
            return;
        }

        if (userRepository.findByEmail(adminEmail).isPresent() ||
            userRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Admin user already exists ({}/{}). Skipping seeding.", adminUsername, adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseGet(() -> {
                    log.warn("ROLE_ADMIN not found, initializing role directly.");
                    return roleRepository.save(new Role(RoleType.ROLE_ADMIN, "Society Management and System Administration"));
                });

        User admin = new User(
                adminUsername,
                adminEmail,
                passwordEncoder.encode(adminPassword),
                adminFullName,
                adminPhone,
                UserStatus.ACTIVE,
                Set.of(adminRole)
        );

        userRepository.save(admin);
        log.info("Successfully seeded default administrator user: email={}, username={}", adminEmail, adminUsername);
    }
}

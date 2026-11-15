-- ====================================================================
-- SocietyOS Seed Data (V2__seed_initial_data.sql)
-- Standard roles for RBAC
-- ====================================================================

INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Society Management and System Administration'),
(2, 'ROLE_RESIDENT', 'Flat Owner or Tenant Resident'),
(3, 'ROLE_SECURITY', 'Gate Security Personnel');

-- Optional baseline admin (Password is Admin@123 with standard BCrypt hash)
-- $2a$10$wK1m10L01U2yZ.1PZ9x2Ce6q1mQW97EeVf/qIqjS44Zp3h8G2rJ2m or verified by CommandLineRunner
INSERT INTO users (id, username, email, password_hash, full_name, phone, status) VALUES
(1, 'admin', 'admin@societyos.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5F0d/h3iV5z0hZ6b4U6Y78P7t6E9a', 'System Administrator', '+919876543210', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1);


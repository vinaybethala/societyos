-- ====================================================================
-- SocietyOS Baseline Database Schema (V1__init_schema.sql)
-- Production-ready MySQL 8.x schema with strict constraints and indexes
-- ====================================================================

-- 1. Roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_roles_name ON roles (name);

-- 2. Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_status ON users (status);

-- 3. User-Roles join table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);
CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);

-- 4. Blocks (Wings/Towers)
CREATE TABLE blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    total_floors INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_blocks_name ON blocks (name);

-- 5. Flats (Apartment units)
CREATE TABLE flats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_id BIGINT NOT NULL,
    flat_number VARCHAR(20) NOT NULL,
    floor_number INT NOT NULL,
    flat_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'VACANT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flats_block FOREIGN KEY (block_id) REFERENCES blocks (id) ON DELETE RESTRICT,
    CONSTRAINT uk_flats_block_number UNIQUE (block_id, flat_number)
);
CREATE INDEX idx_flats_block ON flats (block_id);
CREATE INDEX idx_flats_type ON flats (flat_type);
CREATE INDEX idx_flats_status ON flats (status);

-- 6. Residents (Mapping user to flat as owner or tenant)
CREATE TABLE residents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flat_id BIGINT NOT NULL,
    resident_type VARCHAR(30) NOT NULL,
    move_in_date DATE NOT NULL,
    move_out_date DATE,
    is_primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_residents_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_residents_flat FOREIGN KEY (flat_id) REFERENCES flats (id) ON DELETE RESTRICT
);
CREATE INDEX idx_residents_user ON residents (user_id);
CREATE INDEX idx_residents_flat ON residents (flat_id);
CREATE INDEX idx_residents_status ON residents (status);

-- 7. Vendors (Third-party staff / service contractors)
CREATE TABLE vendors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    total_jobs_completed INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_vendors_service_type ON vendors (service_type);
CREATE INDEX idx_vendors_status ON vendors (status);

-- 8. Maintenance Plans (Billing tariff per flat type)
CREATE TABLE maintenance_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flat_type VARCHAR(30) NOT NULL UNIQUE,
    monthly_amount DECIMAL(10,2) NOT NULL,
    due_day_of_month INT NOT NULL DEFAULT 5,
    late_fee_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    late_fee_grace_days INT NOT NULL DEFAULT 5,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_plans_flat_type ON maintenance_plans (flat_type);

-- 9. Invoices (Monthly maintenance charges per flat)
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flat_id BIGINT NOT NULL,
    maintenance_plan_id BIGINT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    billing_month VARCHAR(7) NOT NULL,
    base_amount DECIMAL(10,2) NOT NULL,
    late_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    due_date DATE NOT NULL,
    late_fee_applied BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_flat FOREIGN KEY (flat_id) REFERENCES flats (id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoices_plan FOREIGN KEY (maintenance_plan_id) REFERENCES maintenance_plans (id) ON DELETE RESTRICT,
    CONSTRAINT uk_invoices_flat_billing_month UNIQUE (flat_id, billing_month)
);
CREATE INDEX idx_invoices_status ON invoices (status);
CREATE INDEX idx_invoices_billing_month ON invoices (billing_month);
CREATE INDEX idx_invoices_due_date ON invoices (due_date);

-- 10. Payments (Audited collection log with idempotency)
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_reference VARCHAR(64) NOT NULL UNIQUE,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'MOCK_UPI',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);
CREATE INDEX idx_payments_invoice ON payments (invoice_id);
CREATE INDEX idx_payments_user ON payments (user_id);
CREATE INDEX idx_payments_paid_at ON payments (paid_at);

-- 11. Complaints (Tickets with SLA)
CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flat_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    assigned_vendor_id BIGINT,
    category VARCHAR(50) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    sla_due_at TIMESTAMP NOT NULL,
    sla_breached BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    resolution_notes TEXT,
    resident_rating INT,
    resident_feedback TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaints_flat FOREIGN KEY (flat_id) REFERENCES flats (id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaints_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaints_vendor FOREIGN KEY (assigned_vendor_id) REFERENCES vendors (id) ON DELETE SET NULL
);
CREATE INDEX idx_complaints_status ON complaints (status);
CREATE INDEX idx_complaints_priority ON complaints (priority);
CREATE INDEX idx_complaints_sla ON complaints (sla_due_at, status);
CREATE INDEX idx_complaints_flat ON complaints (flat_id);
CREATE INDEX idx_complaints_created_by ON complaints (created_by_user_id);

-- 12. Complaint History (Status audit log)
CREATE TABLE complaint_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_history_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE,
    CONSTRAINT fk_complaint_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users (id) ON DELETE RESTRICT
);
CREATE INDEX idx_complaint_history_complaint ON complaint_history (complaint_id);

-- 13. Visitors (Guest and delivery passes)
CREATE TABLE visitors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flat_id BIGINT NOT NULL,
    approved_by_user_id BIGINT NOT NULL,
    visitor_name VARCHAR(100) NOT NULL,
    visitor_phone VARCHAR(20) NOT NULL,
    visitor_type VARCHAR(30) NOT NULL,
    pass_code VARCHAR(20) NOT NULL UNIQUE,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PRE_APPROVED',
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    security_guard_id BIGINT,
    remarks VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_visitors_flat FOREIGN KEY (flat_id) REFERENCES flats (id) ON DELETE RESTRICT,
    CONSTRAINT fk_visitors_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_visitors_security FOREIGN KEY (security_guard_id) REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX idx_visitors_pass_code ON visitors (pass_code);
CREATE INDEX idx_visitors_flat ON visitors (flat_id);
CREATE INDEX idx_visitors_status ON visitors (status);
CREATE INDEX idx_visitors_check_in ON visitors (check_in_time);


# SocietyOS - Entity Relationship Diagram (ERD)

This document visualizes the complete database schema for **SocietyOS**, an apartment and gated-community management system.

## Mermaid ER Diagram

```mermaid
erDiagram
    ROLES ||--o{ USER_ROLES : "assigned to"
    USERS ||--o{ USER_ROLES : "possesses"
    USERS ||--o{ RESIDENTS : "lives as"
    USERS ||--o{ PAYMENTS : "pays"
    USERS ||--o{ COMPLAINTS : "creates"
    USERS ||--o{ COMPLAINT_HISTORY : "audits status"
    USERS ||--o{ VISITORS : "approves"
    USERS ||--o{ VISITORS : "security verifies"

    BLOCKS ||--o{ FLATS : "contains"
    FLATS ||--o{ RESIDENTS : "houses"
    FLATS ||--o{ INVOICES : "billed to"
    FLATS ||--o{ COMPLAINTS : "located at"
    FLATS ||--o{ VISITORS : "destination"

    MAINTENANCE_PLANS ||--o{ INVOICES : "derived from"
    INVOICES ||--o{ PAYMENTS : "settled by"
    VENDORS ||--o{ COMPLAINTS : "assigned to"
    COMPLAINTS ||--o{ COMPLAINT_HISTORY : "tracks transitions"

    ROLES {
        bigint id PK
        varchar name UK "ROLE_ADMIN, ROLE_RESIDENT, ROLE_SECURITY"
        varchar description
        timestamp created_at
        timestamp updated_at
    }

    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        varchar full_name
        varchar phone
        varchar status "ACTIVE, SUSPENDED"
        timestamp created_at
        timestamp updated_at
    }

    USER_ROLES {
        bigint user_id PK,FK
        bigint role_id PK,FK
    }

    BLOCKS {
        bigint id PK
        varchar name UK "e.g. Tower A"
        varchar description
        int total_floors
        timestamp created_at
        timestamp updated_at
    }

    FLATS {
        bigint id PK
        bigint block_id FK
        varchar flat_number "e.g. 101"
        int floor_number
        varchar flat_type "ONE_BHK, TWO_BHK, THREE_BHK, FOUR_BHK, PENTHOUSE"
        varchar status "VACANT, OCCUPIED"
        timestamp created_at
        timestamp updated_at
    }

    RESIDENTS {
        bigint id PK
        bigint user_id FK
        bigint flat_id FK
        varchar resident_type "OWNER, TENANT"
        date move_in_date
        date move_out_date
        boolean is_primary_contact
        varchar status "ACTIVE, MOVED_OUT"
        timestamp created_at
        timestamp updated_at
    }

    VENDORS {
        bigint id PK
        varchar name
        varchar service_type "PLUMBER, ELECTRICIAN, etc."
        varchar phone
        varchar email
        varchar status "ACTIVE, INACTIVE"
        decimal average_rating
        int total_jobs_completed
        timestamp created_at
        timestamp updated_at
    }

    MAINTENANCE_PLANS {
        bigint id PK
        varchar flat_type UK
        decimal monthly_amount
        int due_day_of_month
        decimal late_fee_amount
        int late_fee_grace_days
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    INVOICES {
        bigint id PK
        bigint flat_id FK
        bigint maintenance_plan_id FK
        varchar invoice_number UK
        varchar billing_month "YYYY-MM"
        decimal base_amount
        decimal late_fee
        decimal total_amount
        decimal paid_amount
        varchar status "PENDING, PAID, OVERDUE, CANCELLED"
        date due_date
        boolean late_fee_applied
        bigint version "Optimistic Locking"
        timestamp created_at
        timestamp updated_at
    }

    PAYMENTS {
        bigint id PK
        bigint invoice_id FK
        bigint user_id FK
        varchar payment_reference UK
        varchar idempotency_key UK
        decimal amount
        varchar payment_method
        varchar payment_status "SUCCESS, FAILED"
        timestamp paid_at
        timestamp created_at
    }

    COMPLAINTS {
        bigint id PK
        bigint flat_id FK
        bigint created_by_user_id FK
        bigint assigned_vendor_id FK
        varchar category
        varchar priority "LOW, MEDIUM, HIGH, EMERGENCY"
        varchar title
        text description
        varchar status "OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, ESCALATED"
        timestamp sla_due_at
        boolean sla_breached
        timestamp resolved_at
        timestamp closed_at
        text resolution_notes
        int resident_rating "1 to 5"
        text resident_feedback
        bigint version "Optimistic Locking"
        timestamp created_at
        timestamp updated_at
    }

    COMPLAINT_HISTORY {
        bigint id PK
        bigint complaint_id FK
        bigint changed_by_user_id FK
        varchar from_status
        varchar to_status
        varchar remarks
        timestamp created_at
    }

    VISITORS {
        bigint id PK
        bigint flat_id FK
        bigint approved_by_user_id FK
        varchar visitor_name
        varchar visitor_phone
        varchar visitor_type "GUEST, DELIVERY, CAB, SERVICE, OTHER"
        varchar pass_code UK
        timestamp valid_from
        timestamp valid_to
        varchar status "PRE_APPROVED, PENDING_APPROVAL, CHECKED_IN, CHECKED_OUT, REJECTED, EXPIRED"
        timestamp check_in_time
        timestamp check_out_time
        bigint security_guard_id FK
        varchar remarks
        timestamp created_at
        timestamp updated_at
    }
```

## Relational Constraints & Invariants

1. **Idempotent Billing (`uk_invoices_flat_billing_month`)**:
   - `UNIQUE(flat_id, billing_month)` ensures the monthly billing cron job cannot accidentally generate multiple invoices for the same flat in the same billing cycle.
2. **Idempotent Payments (`uk_payments_idempotency_key`)**:
   - `UNIQUE(idempotency_key)` protects against double charges due to retried network requests.
3. **Flat Uniqueness (`uk_flats_block_number`)**:
   - `UNIQUE(block_id, flat_number)` guarantees flat numbers are unique within each block/tower.
4. **Optimistic Locking (`version`)**:
   - Used on `invoices` and `complaints` to prevent race conditions during concurrent updates (e.g. paying an invoice or multiple admins updating a ticket simultaneously).


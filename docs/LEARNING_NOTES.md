# SocietyOS - Engineering & Interview Learning Notes

---

## Phase 1: Project Setup, Database Schema, and JWT Role-Based Access Control (RBAC)

### 1. What Was Built and Key Design Decisions

#### A. Database Schema Architecture & Flyway Migrations
- **Decision:** Used **Flyway** (`V1__init_schema.sql` and `V2__seed_initial_data.sql`) with Hibernate's `ddl-auto=validate` instead of `update` or `create-drop`.
  * **Why:** In production environments, `ddl-auto=update` can corrupt production schemas, drop columns unintentionally, fail silently on complex constraints, or cause race conditions when multiple application instances start concurrently. Flyway ensures that schema evolution is version-controlled, auditable, deterministic, and repeatable across local development, CI/CD pipelines, staging, and production.
  * **Alternative considered:** Liquibase (uses XML/YAML/JSON/SQL changelogs). Flyway was chosen because raw SQL migrations are transparent, easy to inspect, and directly testable against MySQL.

- **Baseline Schema Invariants:**
  * **Idempotent Invoicing constraint:** Defined a unique composite constraint `uk_invoices_flat_billing_month UNIQUE (flat_id, billing_month)`. Even if two billing threads execute simultaneously, the database enforces that a flat cannot receive two invoices for the same month.
  * **Idempotent Payment constraint:** Defined `uk_payments_idempotency_key UNIQUE (idempotency_key)` to guarantee payments are processed at most once.
  * **Optimistic Locking:** Added `version BIGINT NOT NULL DEFAULT 0` to `invoices` and `complaints` to handle concurrent state modifications without deadlock-prone pessimistic locks.

#### B. Stateless JWT Authentication & Spring Security Architecture
- **Decision:** Stateless authentication using signed JSON Web Tokens (JJWT 0.12.x HMAC-SHA256) and `SessionCreationPolicy.STATELESS`.
  * **Why:** Traditional session-based authentication stores session state in server memory (e.g., `HttpSession`), which makes horizontal scaling difficult without sticky sessions or distributed session caches (like Redis). A signed JWT contains the user identity, roles, and expiration date self-contained within its payload, enabling completely stateless horizontal scalability.
  * **Alternative considered:** Spring Session with Redis. While excellent for distributed stateful sessions, JWT avoids external infrastructure dependencies for authentication while keeping API endpoints standard for mobile and web clients.

- **Role-Based Access Control (RBAC):**
  * Configured `@EnableMethodSecurity` and `hasRole('ADMIN')` on administrative endpoints (`/api/v1/users`).
  * Used `SecurityUtils` to guarantee that current user identity and permissions are strictly read from the verified `SecurityContext` / JWT claims, never trusted from client request parameters.

- **DTOs as Java Records:**
  * **Why:** Records provide immutable data carriers with concise syntax, automatic implementations of `equals()`, `hashCode()`, and `toString()`. Entity models are never exposed directly to prevent security leaks (e.g., password hashes, internal DB IDs, or over-fetching).

- **Centralized Error Handling (`@RestControllerAdvice`):**
  * Catches `MethodArgumentNotValidException`, `BadCredentialsException`, `AccessDeniedException`, and custom domain exceptions (`ResourceNotFoundException`, `ConflictException`, `BadRequestException`) returning a unified `ErrorResponse` record with timestamps, HTTP status, and field-level validation maps.

---

### 2. Five Likely Interview Questions with Model Answers

#### Q1: Why do we disable CSRF protection when building a stateless REST API with JWT?
> **Answer:** Cross-Site Request Forgery (CSRF) exploits how browsers automatically attach cookies to cross-origin requests. In our architecture, the client stores the JWT in client storage (e.g., memory or application storage) and explicitly transmits it via the `Authorization: Bearer <token>` HTTP header. Browsers **never** automatically attach custom `Authorization` headers to cross-origin requests. Since no ambient session cookie is used to authorize the request, CSRF attacks are fundamentally impossible, making CSRF tokens redundant and safe to disable (`csrf.disable()`).

#### Q2: What is the difference between Authentication and Authorization in Spring Security, and where in the FilterChain does each occur?
> **Answer:**
> - **Authentication** answers *"Who are you?"* (verifying credentials such as username/password or token signature).
> - **Authorization** answers *"What are you allowed to do?"* (verifying permissions or roles against the requested resource).
>
> In our filter chain:
> 1. `JwtAuthenticationFilter` intercepts the request before `UsernamePasswordAuthenticationFilter`, parses and cryptographically validates the token signature, and sets the authenticated `UserPrincipal` into `SecurityContextHolder`.
> 2. Downstream authorization checks (configured via `SecurityFilterChain.authorizeHttpRequests` and `@PreAuthorize` method security interceptors) inspect the `GrantedAuthority` collection on the principal to allow or deny access (returning `403 Forbidden` if missing required roles).

#### Q3: How does BCrypt hashing work, and why should we never use SHA-256 or MD5 for passwords?
> **Answer:** Fast hashing algorithms like MD5 or SHA-256 are designed for speed and checksumming data streams; attackers using GPUs can compute billions of SHA-256 hashes per second using precomputed rainbow tables or brute-force dictionaries.
>
> **BCrypt** is an adaptive cryptographic key derivation function based on the Blowfish cipher. It includes:
> 1. An **automatic salt** (16 bytes) embedded directly in the hash string to prevent rainbow table attacks.
> 2. A **configurable work factor (cost)**: with a cost of 10, the algorithm performs $2^{10} = 1024$ iterations, intentionally slowing down brute-force attacks while remaining negligible for a legitimate single login attempt (~50-100ms).

#### Q4: Why should we use Flyway database migrations instead of Hibernate's `spring.jpa.hibernate.ddl-auto=update` in production?
> **Answer:**
> - `ddl-auto=update` cannot handle complex schema modifications (e.g., renaming columns, data backfills, converting column types, modifying foreign keys).
> - It does not track schema version history across different environments, leading to schema drift between developer machines, staging, and production.
> - Multiple application instances restarting at once can attempt simultaneous `ALTER TABLE` statements, resulting in deadlocks or schema corruption.
>
> Flyway records every applied migration in a `flyway_schema_history` table with a checksum and execution timestamp. It executes migrations sequentially in transactional blocks, ensuring reproducible database deployments.

#### Q5: What is the N+1 problem in Spring Data JPA and how does `@EntityGraph` or `JOIN FETCH` resolve it?
> **Answer:** The N+1 problem occurs when fetching an entity with lazy relationships. For example, querying 10 users (`1` query) and subsequently iterating over their roles causes Hibernate to issue `N` additional queries (one per user) to fetch the roles from the `user_roles` table: $1 + 10 = 11$ queries.
>
> We resolve this using `@EntityGraph(attributePaths = {"roles"})` or JPQL `JOIN FETCH u.roles`, which instructs Hibernate to execute a single SQL query using an `INNER JOIN` or `LEFT OUTER JOIN`, loading the user and their associated roles in one round-trip to the database.

---

### 3. Core Concepts to Revise for Interviews
- **JWT Token Lifecycle:** Token structure (Header.Payload.Signature), signing keys (HMAC-SHA256 vs RSA), token expiration, revocation strategies (token blacklisting vs short TTL + refresh tokens).
- **Spring Security Filter Chain:** `OncePerRequestFilter`, `SecurityContextHolder`, `AuthenticationManager`, `AuthenticationProvider`, `UserDetailsService`.
- **Database Indexing:** B-Tree index structure, composite indexes (e.g. `(flat_id, billing_month)`), index cardinality, and avoiding index misses.
- **REST Best Practices:** Proper HTTP status codes (`201 Created` for resource creation, `400` for validation, `401` for unauthenticated, `403` for forbidden, `409` for duplicate unique constraints).


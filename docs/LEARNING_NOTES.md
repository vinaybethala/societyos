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


---

## Phase 2: Society Structure (Blocks, Flats, Residents) and Vendor Management

### 1. What Was Built and Key Design Decisions

#### A. Domain Model & Relational Hierarchy (Blocks -> Flats -> Residents)
- **Decision:** Designed unidirectional `@ManyToOne(fetch = FetchType.LAZY)` associations from `Flat -> Block` and `Resident -> User`, `Resident -> Flat`, avoiding bidirectional `@OneToMany` collections inside `Block` and `Flat`.
  * **Why:** Bidirectional `@OneToMany` collections (e.g. `block.getFlats()`) often cause accidental out-of-memory errors when loading thousands of flats, trigger cascading updates unintentionally, and lead to circular reference serialization loops in REST controllers (`StackOverflowError`). Unidirectional `@ManyToOne` maps foreign keys cleanly and queries are driven through repository methods with explicit pagination (`Pageable`).
  * **Alternative considered:** Bidirectional mappings with `@JsonManagedReference` / `@JsonBackReference`. While viable, keeping entities unidirectional and returning strict DTO records completely decouples the database structure from the API contract.

#### B. Flat Occupancy State Machine & Transactional Invariants
- **Decision:** The occupancy status of a flat (`VACANT` vs `OCCUPIED`) is automatically synchronized inside transactional boundaries during resident lifecycle events:
  * When `assignResident(...)` is invoked: Flat status is atomically updated to `OCCUPIED`.
  * When `recordMoveOut(...)` is invoked: Resident status is changed to `MOVED_OUT`. The service executes an existential check `residentRepository.existsByFlatIdAndStatus(flatId, ACTIVE)`. If no other active residents occupy the flat, flat status atomically reverts to `VACANT`.
  * **Why:** Prevents data anomalies where a flat is marked occupied without active residents, or marked vacant while residents still live there.

#### C. Vendor Management & Soft-Deletion
- **Decision:** Implemented soft-deletion for vendors by transitioning `VendorStatus` to `INACTIVE` rather than executing physical `DELETE` SQL statements.
  * **Why:** In gated-community ecosystems, vendors are assigned to historical complaints, repair tickets, and billing audits. A hard SQL `DELETE` either violates foreign key constraints (`ON DELETE RESTRICT`) or cascades and wipes out historical complaint audits. Soft-deactivation retains audit integrity while preventing future task assignments.
- **Decision:** Used `BigDecimal` for `averageRating` (`precision = 3, scale = 2`) and monetary values.
  * **Why:** Primitive `float` and `double` use binary IEEE-754 floating-point representations that suffer from rounding errors (e.g. `0.1 + 0.2 = 0.30000000000000004`). `BigDecimal` provides exact decimal representation required in financial and metric calculations.

#### D. Performance & Eliminating N+1 Queries
- **Decision:** Annotated repository query methods with `@EntityGraph(attributePaths = {"user", "flat", "flat.block"})`.
  * **Why:** When fetching a paginated list of residents, accessing `resident.getUser()` and `resident.getFlat().getBlock()` without an entity graph issues $1 + N + N = 2N + 1$ SQL queries. `@EntityGraph` triggers a single SQL `LEFT OUTER JOIN`, loading the resident, user, flat, and block in one single round trip.

---

### 2. Five Likely Interview Questions with Model Answers

#### Q1: Why should we prefer `@ManyToOne` over bidirectional `@OneToMany` collections in large enterprise domain models?
> **Answer:** In high-scale applications, a "One" side (like a `Block` or `Society`) can have thousands of child entities (flats, residents, audit logs). A bidirectional `@OneToMany List<Flat> flats` risks:
> 1. Inefficient in-memory loading: Calling `block.getFlats().add(...)` or inspecting size loads every single child flat from the database into JVM memory.
> 2. Collection performance overhead: Hibernate must manage collection dirty checking and persistent bag semantics.
> 3. Unidirectional `@ManyToOne` on the child entity, combined with Spring Data JPA repository pagination (`Page<Flat> findByBlockId(Long blockId, Pageable pageable)`), guarantees constant memory usage regardless of dataset size.

#### Q2: What happens under the hood when a method is annotated with `@Transactional` in Spring?
> **Answer:** Spring creates a dynamic CGLIB or JDK dynamic proxy around the bean.
> 1. When an external call enters the `@Transactional` method, the proxy interceptor opens a connection, binds a `TransactionStatus` to the `ThreadLocal` context, and sets `autoCommit = false` on the JDBC connection.
> 2. If the method completes without an unhandled runtime exception (`RuntimeException` or `Error`), the proxy commits the transaction via the `PlatformTransactionManager`.
> 3. If an uncaught `RuntimeException` or `Error` is thrown, the proxy rolls back all database modifications executed during that call.
>
> *(Note: Self-invocation—calling a `@Transactional` method from another method within the same class—bypasses the proxy, so transaction interceptors are not invoked!)*

#### Q3: What is the difference between Lazy and Eager fetching in JPA, and why is `FetchType.EAGER` considered an anti-pattern on collections?
> **Answer:**
> - `FetchType.EAGER` instructs Hibernate to immediately load the associated entity or collection whenever the parent entity is fetched.
> - `FetchType.LAZY` injects a bytecode proxy or persistent collection wrapper, deferring the SQL query until the relationship getter is first invoked in code.
>
> `FetchType.EAGER` on collections (`@OneToMany`, `@ManyToMany`) is considered an anti-pattern because:
> 1. It forces queries to always load child collections even when the business logic doesn't need them, wasting CPU, memory, and database bandwidth.
> 2. When executing JPQL `SELECT b FROM Block b`, Hibernate cannot easily optimize eager collections in the primary query and issues $N$ separate sub-queries, creating the N+1 problem by default.
> 3. Best practice is to keep relationships `LAZY` and selectively use `@EntityGraph` or `JOIN FETCH` only for endpoints that need the child data.

#### Q4: How do you handle Soft Deletes in Spring Boot and what are the trade-offs?
> **Answer:** Soft deletion flags records as inactive (e.g. `status = 'INACTIVE'` or a timestamp `deleted_at`) instead of issuing a SQL `DELETE`.
> - **Pros:** Preserves foreign key relationships, audit trails, and historical reporting; allows easy undelete.
> - **Cons:**
>   1. Unique constraints become tricky: e.g., if phone is unique, an inactive user prevents a new user from registering that same phone unless unique indexes include status or partial indexes are used.
>   2. Queries must filter out inactive records (using `@Where(clause = "status = 'ACTIVE'")` or explicit repository conditions), otherwise deactivated rows appear in business logic.

#### Q5: What is the difference between `findById(...)` and `getReferenceById(...)` in Spring Data JPA?
> **Answer:**
> - `findById(id)` executes an immediate SQL `SELECT` statement and returns an `Optional<T>` containing the initialized entity (or empty if not found).
> - `getReferenceById(id)` (formerly `getOne`) does **not** hit the database immediately. Instead, it delegates to Hibernate's `EntityManager.getReference()`, returning a lazy proxy with only the ID initialized. The database is only queried if a non-ID property is accessed.
> - **Use case:** `getReferenceById` is ideal when creating a foreign key reference (e.g., setting a `flat` reference on an invoice or resident) without paying the performance cost of a `SELECT` query when we know the ID exists.

---

### 3. Core Concepts to Revise for Interviews
- **JPA Entity States:** Transient (new), Managed/Persistent (attached to session), Detached (session closed), and Removed.
- **Dirty Checking:** How Hibernate compares snapshot states at flush time and automatically issues SQL `UPDATE` without calling `save()`.
- **Composite Unique Constraints:** `UNIQUE (block_id, flat_number)` preventing duplicate flats per tower at the database engine level.
- **Spring Security Method Security:** `@PreAuthorize("hasRole('ADMIN')")` vs `@PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT')")`.

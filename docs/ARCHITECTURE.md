# SpringMate Backend Architecture

> **Note**: For system-level architecture (System Context and Container diagrams), see `mm-infra/docs/ARCHITECTURE.md`.

---

## High-Level Architecture

The SpringMate backend follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                    │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Controllers Layer                     │   │
│  │  (REST endpoints, request validation, response mapping)  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Services Layer                        │   │
│  │  (Business logic, orchestration, transaction management) │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Repository Layer                      │   │
│  │  (Data access, JPA repositories, custom queries)         │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Infrastructure Layer                  │   │
│  │  (Security, caching, external services, filters)         │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌─────────────────┐    ┌──────────────┐
│  PostgreSQL  │    │    Redis        │    │    AWS S3    │
│  (Primary DB)│    │ (Cache + Auth   │    │(File Storage)│
│              │    │  session cache) │    │              │
└──────────────┘    └─────────────────┘    └──────────────┘
```

### Key Architectural Decisions

- **Layered Architecture**: Clear separation between controllers, services, and repositories enables testability and maintainability
- **JWT + Server-Side Session**: A signed JWT carries the `sessionId` (JWT subject). Sessions are persisted in PostgreSQL and validated via a Redis auth cache for fast lookups.
- **Redis Dual Purpose**: Used for Spring Cache and for auth/session validation caching.
- **Presigned URLs**: S3 presigned URLs generated server-side for secure, direct client uploads
- **Filter Chain**: Request-level concerns (request-id correlation, security headers, rate limiting, authentication) handled via servlet filters before controller layer
- **Consistent API URLs**: Endpoints are centrally defined in `com.example.SpringMate.Shared.Urls`

---

## Domain Architecture

SpringMate is organized by business capability. Each domain maps to one or more packages under `com.example.SpringMate`.

### Authentication

- **Purpose**: User login, logout, OTP flows, email verification, and JWT/cookie issuance.
- **Responsibilities**: Password and OTP authentication; OTP dispatch; email verification OTP; auth details; authenticated session resolve (`/api/v1/auth/resolve_session`).
- **Key components**: `AuthController`, `AuthenticationFilter`, `AuthService`, `OtpNotificationDispatcher`, `JwtTokenProvider`, `AuthHelper`, `VerificationCode` / `VerificationCodeRepository`.

### Sessions

- **Purpose**: Server-side session lifecycle tied to JWT subject (`sessionId`).
- **Responsibilities**: Create and invalidate sessions; persist session records; validate sessions per request; cache validated sessions in Redis.
- **Key components**: `Session`, `SessionLog`, `SessionRepository`, `SessionLogRepository`, `SessionManagementHelper`, `SessionHelper`, `SessionValidationService`, `SessionAuthenticationFilter`, `AuthCacheService`, `CachedAuthentication`.

### Verification Codes

- **Purpose**: Store and validate time-bound OTP and email-verification codes.
- **Responsibilities**: Persist verification codes; support login OTP and email verification flows initiated by `AuthService`.
- **Key components**: `VerificationCode`, `VerificationCodeRepository`, `AuthService`, `OtpNotificationDispatcher`.

### User Management

- **Purpose**: User registration, profile management, and Spring Security user loading.
- **Responsibilities**: Create/update/delete users; profile image fallback upload; expose user details; load users for authentication.
- **Key components**: `UserController`, `UserService`, `CoreUserService`, `UserDetailServiceImpl`, `User`, `Role`, `UserRepository`, `RoleRepository`.

### Listings

- **Purpose**: Marketplace listing CRUD, browse/search, seller contact, and image handling.
- **Responsibilities**: Create/update/delete listings; browse with filters and projections; suggest listings; contact seller via email; manage listing images and conditions.
- **Key components**: `ListingController`, `ListingService`, `Listing`, `ListingImage`, `Condition`, `ContactMessage`, `ListingRepository`, `ListingImageRepository`, `ConditionRepository`, `ContactMessageRepository`.

### Categories

- **Purpose**: Read-only category reference data for listings.
- **Responsibilities**: Return all active categories; cache category list in Redis.
- **Key components**: `CategoryController`, `CategoryService`, `Category`, `CategoryRepository` (all under the `Listing` package).

### Locations

- **Purpose**: Geographic reference data (countries, states, cities) for listings and seeding.
- **Responsibilities**: Serve location hierarchies; seed location data from external API; cache country/state/city lookups.
- **Key components**: `LocationController`, `LocationService`, `LocationSeederService`, `Country`, `State`, `City`, `Location`.

### Favorites

- **Purpose**: Per-user listing favorites.
- **Responsibilities**: Add/remove favorites; enforce user ownership of favorite actions.
- **Key components**: `UserFavoriteController`, `UserFavoriteService`, `UserFavorite`, `UserFavoriteRepository`.

### Storage

- **Purpose**: S3 object storage for listing and profile images.
- **Responsibilities**: Generate presigned PUT/GET URLs; verify object existence; direct upload fallback via `AwsS3Service`; cleanup on listing failure.
- **Key components**: `StorageController`, `StorageService`, `AwsS3Service`.

### Admin

- **Purpose**: Admin-only moderation and user management APIs.
- **Responsibilities**: List/update/delete users; list/delete listings; soft-delete and restore operations (protected by `@PreAuthorize("hasRole('ADMIN')")`).
- **Key components**: `AdminController`, `UserService`, `ListingService`.

### Internal APIs

- **Purpose**: Service-to-service endpoints not intended for browser clients.
- **Responsibilities**: Resolve `sessionId` → `userUuid` for the chat service; authenticate callers via `X-SERVICE-KEY`.
- **Key components**: `InternalAuthController`, `SessionValidationService` (`/internal/v1/auth/resolve_session`).

### Monitoring

- **Purpose**: Operational metrics and health probes.
- **Responsibilities**: Expose Actuator endpoints; protect `/actuator/prometheus` with a dedicated `PROMETHEUS` role and HTTP Basic auth; seed Prometheus system user.
- **Key components**: `SecurityConfig` (`prometheusChain`), Spring Boot Actuator, `UserSeeder`, Micrometer Prometheus registry.

### Health

- **Purpose**: Lightweight application health endpoint for load balancers and Compose.
- **Responsibilities**: Public `/health` status check (also listed in `Urls.Security.PUBLIC_ENDPOINTS`).
- **Key components**: `HealthController`.

### Shared Services (cross-domain)

- **Purpose**: Cross-cutting infrastructure used by multiple domains.
- **Responsibilities**: Transactional email (AWS SES); email templates; global exception handling; URL constants; input sanitization.
- **Key components**: `EmailService`, `EmailTemplateService`, `GlobalExceptionHandler`, `Urls`, `Constants`, `InputSanitizer`.

---

## Package Structure

High-level layout of `com.example.SpringMate`:

```
com.example.SpringMate
├── Admin          # Admin moderation APIs
├── Auth           # Authentication, sessions, verification codes, internal auth
├── Config         # Spring configuration (security, Redis, AWS, JPA, async, cache)
├── Favorite       # User listing favorites
├── Filter         # Servlet filters (request ID, security headers, rate limiting, logging)
├── Health         # Public health endpoint
├── Listing        # Listings, categories, conditions, contact messages, images
├── Location       # Countries, states, cities, location seeding
├── Seeder         # Startup data seeders (roles, users, conditions, locations)
├── Shared         # URLs, constants, email, exceptions, helpers
├── Storage        # S3 presigned URLs and direct upload
├── User           # User CRUD and Spring Security UserDetailsService
└── Util           # API response wrappers, authenticated principal, pagination helpers
```

| Package | Purpose | Responsibilities |
|---------|---------|------------------|
| `Admin` | Administrative APIs | User and listing moderation behind `ADMIN` role |
| `Auth` | Identity and session domain | Login/logout/OTP, JWT, sessions, verification codes, internal session resolve |
| `Config` | Application wiring | `SecurityConfig`, Redis/cache/AWS/JPA/async/thread-pool beans, `AppProperties` |
| `Favorite` | Favorites feature | Persist and expose user ↔ listing favorites |
| `Filter` | Request pipeline | Correlation ID, security headers, rate limiting, request logging |
| `Health` | Health checks | Public `/health` endpoint |
| `Listing` | Core marketplace domain | Listings, images, conditions, categories, seller contact messages |
| `Location` | Reference geography | Location hierarchy APIs and external API seeding |
| `Seeder` | Bootstrap data | `CommandLineRunner` seeders for roles, Prometheus user, conditions, locations |
| `Shared` | Cross-cutting utilities | Centralized URLs, email delivery, global errors, shared enums/helpers |
| `Storage` | File storage integration | S3 presign and upload abstraction |
| `User` | User domain | Registration, profile updates, role association, `UserDetailsService` |
| `Util` | API infrastructure | Standard `Response<T>` wrapper, `AuthenticatedUser`, mappers |

Each domain package typically follows `Controller` → `Service` → `Repository` → `Entity` (plus `DTO` where needed). `Auth` additionally contains `Filter`, `Cache`, `Helper`, and `jwt` subpackages.

---

## Request Lifecycle

A typical authenticated API request passes through servlet filters, Spring Security, controller layers, and infrastructure backends.

```
HTTP Request
  → RequestIdFilter              (correlation ID → MDC + response header)
  → SecurityHeadersFilter        (HSTS, CSP, X-Frame-Options, etc.)
  → RateLimitingFilter           (Resilience4j rate limiter by path)
  → Spring Security FilterChainProxy
       ├─ prometheusChain        (/actuator/prometheus only — HTTP Basic, PROMETHEUS role)
       └─ appChain               (all other requests)
            → SessionAuthenticationFilter   (JWT from cookie or Authorization header)
            → AuthenticationFilter          (password login URL only)
            → Spring Security authorization
  → RequestLoggingFilter         (REQUEST_START / REQUEST_END timing)
  → DispatcherServlet
  → Controller
  → Service
  → Repository / Redis cache / AWS SDK
  → PostgreSQL / Redis / AWS S3 / AWS SES
  → Response<T> JSON
  → GlobalExceptionHandler       (on uncaught exceptions)
```

### Servlet filters (application-wide)

Registered as `@Component` `OncePerRequestFilter` beans with explicit `@Order`:

| Order | Filter | Responsibility |
|-------|--------|----------------|
| `HIGHEST_PRECEDENCE` | `RequestIdFilter` | Reads or generates `X-Request-Id`; stores in SLF4J MDC; echoes header on response |
| `HIGHEST_PRECEDENCE + 1` | `SecurityHeadersFilter` | Adds HSTS, `X-Frame-Options`, `X-Content-Type-Options`, `X-XSS-Protection`, CSP, `Referrer-Policy` from `AppProperties` |
| `HIGHEST_PRECEDENCE + 2` | `RateLimitingFilter` | Applies Resilience4j limiters: `auth` (auth paths), `public` (category/location/browse), `api` (default) |
| `LOWEST_PRECEDENCE` (default) | `RequestLoggingFilter` | Logs request start/end with status and duration |

### Spring Security filter chains

`SecurityConfig` defines two `@Order`-ed chains:

1. **`prometheusChain` (`@Order(1)`)** — matches `/actuator/prometheus`; HTTP Basic auth; requires `PROMETHEUS` role.
2. **`appChain` (`@Order(2)`)** — all other requests; stateless sessions; CORS enabled; CSRF disabled.

Inside `appChain`, filters are added before `UsernamePasswordAuthenticationFilter`:

1. **`SessionAuthenticationFilter`** — extracts `auth_token` cookie or `Authorization: Bearer` token; validates JWT; calls `SessionValidationService.validate(sessionId)` (Redis → PostgreSQL); populates `SecurityContextHolder`. Skipped for paths in `Urls.Security.FILTER_EXCLUDED_ENDPOINTS`.
2. **`AuthenticationFilter`** — handles `POST /api/v1/auth/login_with_password` only; on success creates session, caches auth, issues JWT cookie.

Authorization rules: `Urls.Security.PUBLIC_ENDPOINTS` are permit-all; `/actuator/**` requires `SUPER_ADMIN`; all other requests require authentication.

### Downstream processing

- **Controller**: Validates request DTOs (`@Valid`), maps to service calls, returns `ResponseEntity<Response<T>>`.
- **Service**: Business logic, transaction boundaries, authorization checks (e.g. resource ownership), orchestration of repositories and external services.
- **Repository**: Spring Data JPA repositories for PostgreSQL; Redis accessed via `RedisTemplate` / `@Cacheable` abstractions.
- **Exception handling**: `GlobalExceptionHandler` (`@RestControllerAdvice`) maps domain and framework exceptions to consistent `Response<T>` error payloads and HTTP status codes.

---

## Technology Stack

### Core Framework
- **Spring Boot**: 3.5.9 with Java 17 (`pom.xml` `<java.version>`)
- **Spring Security**: Stateless filter chain + method-level security (`@EnableMethodSecurity`)
- **Spring Data JPA**: Hibernate with PostgreSQL
- **Spring Cache**: Redis-backed cache abstraction (`spring-boot-starter-cache` + `spring-boot-starter-data-redis`)
- **JWT**: JJWT 0.13.0 (`io.jsonwebtoken:jjwt-*`)

### External Services
- **AWS S3 / SES**: AWS SDK for Java v2 (BOM `2.30.0`) — S3 presigned URLs and SES email delivery
- **Redis**: Auth-session lookup cache (`AuthCacheService`) and Spring Cache (`@Cacheable`); not the session system of record
- **PostgreSQL**: Primary relational database; sessions and domain data are persisted here

#### Email Delivery (AWS SES)

- Emails are sent using AWS SES via the AWS SDK v2.
- Emails are transactional only (OTP, verification, user notifications).
- Email sending is executed asynchronously using `@Async("appDefault")`.
- Resilience4j retry and circuit breaker patterns are applied to SES calls.

### Infrastructure
- **Docker**: Containerization for development and deployment
- **Actuator + Prometheus**: Metrics endpoint (`/actuator/prometheus`) via `micrometer-registry-prometheus`
- **Resilience4j**: 2.3.0 — circuit breakers, retries, and rate limiting for external calls
- **Logback**: Structured JSON logging in production (`logstash-logback-encoder`)

---

## Key Data Flows

### Authentication Flow

```
Client → AuthenticationFilter (/api/v1/auth/login_with_password)
          OR AuthController (/api/v1/auth/login_with_otp)
                              ↓
      OtpNotificationDispatcher → EmailService → AWS SES
                              ↓
                      SessionManagementHelper (create session)
                              ↓
               SessionRepository → PostgreSQL (persist session)
                              ↓
        AuthCacheService → Redis (auth:session:<sessionId> with TTL)
                              ↓
        JwtTokenProvider (JWT subject=sessionId)
                              ↓
        AuthHelper injects `auth_token` cookie → Client
```

**Runtime validation**: `SessionAuthenticationFilter` accepts either `Authorization: Bearer <token>` or the `auth_token` cookie, validates JWT signature/expiry, then validates session via `SessionValidationService` (Redis cache → PostgreSQL fallback).

### Listing Creation Flow

```
Client → StorageController (/api/v1/storage/presign_url)
                              ↓
                    StorageService → S3Presigner (PUT presign)
                              ↓
                    Client uploads directly to S3 (PUT)
                              ↓
Client → ListingController (/api/v1/listing/create) → ListingService
                              ↓
                 ListingRepository → PostgreSQL (save listing + image object keys)
                              ↓
                 (on failure) StorageService.deleteImage() (cleanup)
```

**Fallback path**: The API also supports a multipart image upload fallback (`/api/v1/listing/image_upload_fallback`) which uploads via backend using `AwsS3Service.uploadImage(...)` with retry + circuit breaker.

### Listing Retrieval Flow

```
Client → ListingController → ListingService
                              ↓
    ListingRepository → PostgreSQL (projection query with filters)
                              ↓
    StorageService → S3Presigner (generate presigned GET URL for cover image)
                              ↓
                    Response → Client
```

**Caching note**: Redis caching is used for relatively static reference data (categories/locations/conditions) and for auth/session validation caching. Listing browse queries are executed via projection queries and are not cached at the service layer in this repo.

---

## Security Architecture

### Authentication

- **JWT transport**: Token is accepted from either the `auth_token` cookie or the `Authorization` header.
- **JWT contents**: JWT subject is the `sessionId`, enabling server-side session invalidation without token rotation.
- **Session persistence + cache**: Session records are stored in PostgreSQL; Redis caches `CachedAuthentication` for fast validation.
- **OTP Verification**: Email-based OTP for login adds an additional security layer.

**Session validation**: `SessionValidationService` checks Redis first, then falls back to PostgreSQL and caches the result with TTL until `expiresAt`.

### Authorization

- **Role-Based Access Control (RBAC)**: Roles include `USER`, `ADMIN`, `SUPER_ADMIN`, and a dedicated `PROMETHEUS` role for metrics.
- **Method-level security**: Admin endpoints are protected via `@PreAuthorize("hasRole('ADMIN')")`.
- **Resource-Level Security**: Users can only modify their own resources (enforced in service layer)
- **Filter Chain Security**: Request-id correlation, security headers, rate limiting, and authentication checks run before controllers.
- **Protected metrics**: `/actuator/prometheus` is protected by a dedicated `SecurityFilterChain` using HTTP Basic auth.

### Security Headers

Filter chain automatically adds security headers to all responses:
- HSTS (HTTP Strict Transport Security)
- X-Frame-Options, X-Content-Type-Options
- X-XSS-Protection
- Content-Security-Policy
- Referrer-Policy

---

## Performance & Scalability

### Caching Strategy

- **Categories**: Cached in Redis (`Constants.CacheNamespace.CATEGORY`, key `'all'`)
- **Locations**: Cached by country/state (`COUNTRY`, `STATE`, `CITY` caches)
- **Conditions**: Cached in Redis (`CONDITION`, key `'active'`)
- **Auth/session validation**: Cached in Redis with key prefix `auth:session:` and TTL derived from session expiry

**Cache TTL**: Default cache TTL is configured at 30 minutes via `RedisCacheManager`.

### Database Optimization

- **Strategic Indexes**: Some entities define indexes/constraints. See `docs/DATABASE_INDEXES.md`.
- **Projection Queries**: Listing browse endpoints use projection queries (`FetchListingItemsProjection`) to avoid loading full entities.
- **Batch Operations**: JDBC batch size configured (20) for bulk inserts

### Scalability Considerations

- **Stateless request handling**: HTTP sessions are not used; authentication is validated per request via filters.
- **Connection Pooling**: HikariCP connection pool configured for optimal database connection management
- **Async Processing**: OTP and transactional email dispatch run asynchronously and are sent via AWS SES with retry and circuit breaker resilience.
- **Response Compression**: GZIP compression reduces bandwidth by 60-80% for JSON responses

---

## Runtime Configuration

Configuration is externalized in YAML and environment variables — not hardcoded in domain code.

- **Organization**: Base settings in `src/main/resources/application.yml`; profile-specific overrides in `application-local.yml` and `application-prod.yml`. Typed app settings are bound via `AppProperties` (`@ConfigurationProperties`). Secrets and connection strings are loaded from `src/main/java/com/example/SpringMate/env/.env` (local) via Spring's env file support.
- **Profiles**: `local` enables HTTPS, debug logging, and development CORS/cookie domains. `prod` enforces schema validation (`ddl-auto: validate`) and production-oriented settings. Activate with `SPRING_PROFILES_ACTIVE`.
- **Environment variables**: Database (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), auth (`JWT_SECRET`, `SESSION_VALIDITY_DAYS`), AWS credentials, `PROMETHEUS_PASS`, `SERVICE_KEY`, and other secrets are injected from `.env` or the deployment platform.

### Local HTTPS

The `local` profile requires HTTPS (`server.ssl.enabled: true` in `application-local.yml`). Spring Boot loads a PKCS12 keystore (`marketmate-local.p12`) from `src/main/resources/`. Certificate generation and keystore setup are documented in **`docs/README-SSL.md`** — do not commit keystore files.

For property-level detail, defaults, and per-environment values, see **`docs/CONFIGURATION.md`**.

---

## Database Design

PostgreSQL is the system of record for all persistent domain data in the `marketmate` database.

- **JPA/Hibernate**: Entities use Spring Data JPA with Hibernate; auditing (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`) is enabled via `JpaConfig` and `AuditingEntityListener`. Local profile uses `ddl-auto: update`; production uses `ddl-auto: validate`.
- **Entity relationships**: Core graph includes `User` ↔ `Listing` (seller), `Listing` → `Category`, `Location`, `Condition`, `ListingImage`; `User` ↔ `UserFavorite` ↔ `Listing`; `Session` → `User`; `VerificationCode` supports auth flows. Listing browse queries use projections (`FetchListingItemsProjection`) to limit loaded columns.
- **Index strategy**: Indexes and unique constraints are declared on JPA entities (e.g. `sessions.session_id`, `users.email`, `users.uuid`). Additional recommended indexes and query-pattern analysis are maintained separately.

For the full index catalog, per-table constraints, and optimization notes, see **`docs/DATABASE_INDEXES.md`**.

---

## Related Documentation

- **System Architecture**: `mm-infra/docs/ARCHITECTURE.md` (platform-level diagrams, Docker Compose, local HTTPS for frontends/chat)
- **Configuration**: `docs/CONFIGURATION.md` (application properties and environment variables)
- **Database Indexes**: `docs/DATABASE_INDEXES.md` (index and constraint catalog)
- **Local HTTPS (backend)**: `docs/README-SSL.md` (PKCS12 keystore setup for the `local` profile)

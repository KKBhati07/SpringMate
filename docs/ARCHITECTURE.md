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

## Technology Stack

### Core Framework
- **Spring Boot**: 3.5.x with Java 21
- **Spring Security**: Stateless filter chain + method-level security 
- **Spring Data JPA**: Hibernate with PostgreSQL
- **Spring Cache**: Redis abstraction layer

### External Services
- **AWS S3**: File storage with presigned URL pattern
- **Redis**: Caching and session storage
- **PostgreSQL**: Primary relational database
- **SMTP**: Email service for OTP and notifications

### Infrastructure
- **Docker**: Containerization for development and deployment
- **Actuator + Prometheus**: Metrics endpoint (`/actuator/prometheus`) via Micrometer registry
- **Resilience4j**: Circuit breakers and retries for external calls
- **Logback**: Structured logging (JSON in production)

---

## Key Data Flows

### Authentication Flow

```
Client → AuthenticationFilter (/api/v1/auth/login_with_password)
          OR AuthController (/api/v1/auth/login_with_otp)
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
- **Async Processing**: OTP email dispatch and S3 deletes run via `@Async("appDefault")` executor.
- **Response Compression**: GZIP compression reduces bandwidth by 60-80% for JSON responses

---

## Related Documentation

- **System Architecture**: `mm-infra/docs/ARCHITECTURE.md` (system-level diagrams)
- **Configuration**: `docs/CONFIGURATION.md` (configuration properties)
- **Database Indexes**: `docs/DATABASE_INDEXES.md` (index documentation)

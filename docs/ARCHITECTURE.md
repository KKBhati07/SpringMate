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
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  PostgreSQL  │    │    Redis      │    │    AWS S3     │
│  (Primary DB)│    │  (Cache/Session)│  │  (File Storage)│
└──────────────┘    └──────────────┘    └──────────────┘
```

### Key Architectural Decisions

- **Layered Architecture**: Clear separation between controllers, services, and repositories enables testability and maintainability
- **Dual Authentication**: JWT tokens in httpOnly cookies + Redis-backed sessions for stateless scalability
- **Redis Dual Purpose**: Used for both caching (categories, locations) and session storage to reduce database load
- **Presigned URLs**: S3 presigned URLs generated server-side for secure, direct client uploads
- **Filter Chain**: Request-level concerns (rate limiting, security headers, authentication) handled via filter chain before controller layer

---

## Technology Stack

### Core Framework
- **Spring Boot 3.x** with Java 17+
- **Spring Security**: JWT + Session-based authentication
- **Spring Data JPA**: Hibernate with PostgreSQL 15
- **Spring Cache**: Redis abstraction layer

### External Services
- **AWS S3**: File storage with presigned URL pattern
- **Redis**: Caching and session storage
- **PostgreSQL**: Primary relational database
- **SMTP**: Email service for OTP and notifications

### Infrastructure
- **Docker**: Containerization for development and deployment
- **Prometheus + Grafana**: Metrics and monitoring
- **Resilience4j**: Circuit breakers and retries for external calls
- **Logback**: Structured logging (JSON in production)

---

## Key Data Flows

### Authentication Flow

```
Client → AuthController → AuthService
                              ↓
                    UserRepository → PostgreSQL (validate user)
                              ↓
                    SessionRepository → PostgreSQL (create session)
                              ↓
                    Redis (cache session for fast lookup)
                              ↓
                    JWT in httpOnly cookie → Client
```

**Design Decision**: Sessions stored in both PostgreSQL (persistence) and Redis (performance). Redis provides fast session validation while PostgreSQL ensures durability.

### Listing Creation Flow

```
Client → ListingController → ListingService
                              ↓
                    ListingRepository → PostgreSQL (save metadata)
                              ↓
                    StorageService → AWS S3 (generate presigned URLs)
                              ↓
                    Client uploads directly to S3
                              ↓
                    StorageService → S3 (verify upload, update listing)
```

**Design Decision**: Presigned URLs allow direct client-to-S3 uploads, reducing server bandwidth and improving upload performance. Server maintains control through URL expiration and validation.

### Listing Retrieval Flow (with Caching)

```
Client → ListingController → ListingService
                              ↓
                    Redis Cache (check for cached result)
                              ↓ (cache miss)
                    ListingRepository → PostgreSQL (query with filters)
                              ↓
                    Redis Cache (store result)
                              ↓
                    StorageService → S3 (generate presigned URLs for images)
                              ↓
                    Response → Client
```

**Design Decision**: Multi-level caching strategy - Redis for frequently accessed data (categories, locations), database queries cached for expensive operations. Presigned URLs generated on-demand to ensure security.

---

## Security Architecture

### Authentication

- **JWT in httpOnly Cookies**: Tokens stored in httpOnly cookies prevent XSS attacks. Frontend JavaScript cannot access tokens.
- **Redis Session Cache**: Session validation happens in Redis for performance, with PostgreSQL as source of truth.
- **OTP Verification**: Email-based OTP for login adds an additional security layer.

**Security Decision**: Dual storage (PostgreSQL + Redis) balances security (durable session records) with performance (fast validation).

### Authorization

- **Role-Based Access Control (RBAC)**: ADMIN and USER roles with method-level security (`@PreAuthorize`)
- **Resource-Level Security**: Users can only modify their own resources (enforced in service layer)
- **Filter Chain Security**: Rate limiting, security headers, and authentication checks at filter level before reaching controllers

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

- **Categories**: Cached in Redis (rarely changes, high read frequency)
- **Locations**: Cached by country/state (frequently accessed, hierarchical data)
- **Query Results**: Expensive queries cached with TTL based on data volatility

**Decision**: Spring Cache abstraction allows switching cache providers without code changes. Redis chosen for distributed caching support (horizontal scaling).

### Database Optimization

- **Strategic Indexes**: Indexes on frequently queried columns (user UUID, listing status, location IDs)
- **EntityGraph**: Prevents N+1 query problems by eager loading relationships
- **Projection Queries**: DTO projections for listing lists to avoid loading full entities
- **Batch Operations**: JDBC batch size configured (20) for bulk inserts

### Scalability Considerations

- **Stateless Design**: JWT + Redis sessions enable horizontal scaling (no sticky sessions required)
- **Connection Pooling**: HikariCP connection pool configured for optimal database connection management
- **Async Processing**: Email sending and non-critical operations handled asynchronously
- **Response Compression**: GZIP compression reduces bandwidth by 60-80% for JSON responses

---

## Related Documentation

- **System Architecture**: `mm-infra/docs/ARCHITECTURE.md` (system-level diagrams)
- **Configuration**: `docs/CONFIGURATION.md` (configuration properties)
- **Database Indexes**: `docs/DATABASE_INDEXES.md` (index documentation)

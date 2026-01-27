# SpringMate Backend Architecture Documentation (C4 Model)

This document provides detailed architecture diagrams for the SpringMate backend application using the C4 model (Component and Code levels).

> **Note**: For system-level architecture (System Context and Container diagrams), see `mm-infra/docs/ARCHITECTURE.md`.

---

## Table of Contents

1. [Component Diagram (Level 3)](#component-diagram-level-3)
2. [Code Diagram (Level 4)](#code-diagram-level-4)
3. [Technology Stack](#technology-stack)
4. [Data Flow Examples](#data-flow-examples)
5. [Security Architecture](#security-architecture)
6. [Performance Optimizations](#performance-optimizations)

---

## Component Diagram (Level 3)

### Overview
The Component diagram shows the major components within the Spring Boot application.

```
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                    │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Controllers Layer                     │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Auth       │  │   Listing     │  │   User       │   │   │
│  │  │ Controller  │  │  Controller  │  │  Controller  │     │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Admin      │  │   Location   │  │   Category   │    │   │
│  │  │  Controller  │  │  Controller  │  │  Controller  │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐                      │   │
│  │  │   Favorite   │  │   Storage    │                      │   │
│  │  │  Controller  │  │  Controller  │                      │   │
│  │  └──────────────┘  └──────────────┘                      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Services Layer                        │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Auth       │  │   Listing     │  │   User       │   │   │
│  │  │   Service    │  │   Service     │  │   Service    │   │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Location   │  │   Category   │  │   Storage    │    │   │
│  │  │   Service    │  │   Service    │  │   Service    │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Favorite   │  │   Email      │  │   AwsS3      │    │   │
│  │  │   Service    │  │   Service    │  │   Service    │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Repository Layer                      │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   User       │  │   Listing     │  │   Category   │   │   │
│  │  │  Repository  │  │  Repository  │  │  Repository  │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Location   │  │   Session    │  │   Favorite   │    │   │
│  │  │  Repository  │  │  Repository  │  │  Repository  │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Listing    │  │   Verification│ │   Role       │    │   │
│  │  │   Image      │  │   Code        │  │  Repository  │   │   │
│  │  │  Repository  │  │  Repository  │  │              │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Infrastructure Layer                  │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Security   │  │   Redis      │  │   Cache      │    │   │
│  │  │   Config     │  │   Config     │  │   Config     │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   AWS        │  │   JPA        │  │   Async      │    │   │
│  │  │   Config     │  │   Config     │  │   Config     │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Filter     │  │   Exception  │  │   Thread     │    │   │
│  │  │   Chain      │  │   Handler     │  │   Pool       │   │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### Key Components

#### Controllers
- **AuthController**: Handles authentication (login, OTP, session management)
- **ListingController**: Manages listing CRUD operations with pagination
- **UserController**: Handles user profile operations
- **AdminController**: Admin-specific operations (user/listings management)
- **LocationController**: Location data endpoints (countries, states, cities)
- **CategoryController**: Category listing endpoints
- **UserFavoriteController**: Manages user favorite listings (toggle favorites)
- **StorageController**: Handles S3 presigned URLs and object existence checks

#### Services
- **AuthService**: Authentication and session management logic
- **ListingService**: Listing business logic, filtering, pagination
- **UserService**: User management and profile operations
- **CoreUserService**: Core user operations (get, validate)
- **LocationService**: Location data retrieval and caching
- **LocationSeederService**: Seeds location data (countries, states, cities)
- **CategoryService**: Category operations with caching
- **StorageService**: AWS S3 file operations (presigned URLs, upload, delete)
- **AwsS3Service**: Low-level AWS S3 client operations
- **UserFavoriteService**: Favorite listings management
- **EmailService**: Email sending (OTP, notifications)
- **EmailTemplateService**: Email template management
- **AuthCacheService**: Authentication cache operations (Redis)
- **UserDetailServiceImpl**: Spring Security UserDetailsService implementation

#### Repositories
- **UserRepository**: User data access
- **RoleRepository**: User role data access
- **ListingRepository**: Listing data access with complex queries
- **ListingImageRepository**: Listing image data access
- **CategoryRepository**: Category data access
- **LocationRepository**: Location data access (base)
- **CountryRepository**: Country data access
- **StateRepository**: State data access
- **CityRepository**: City data access
- **SessionRepository**: Session data access
- **SessionLogRepository**: Session log data access
- **UserFavoriteRepository**: User favorites data access
- **VerificationCodeRepository**: OTP verification code data access

#### Infrastructure
- **SecurityConfig**: Spring Security configuration (includes CORS)
- **RedisConfig**: Redis connection configuration
- **RedisCacheManagerConfig**: Redis cache manager configuration
- **CacheConfig**: Spring Cache abstraction configuration
- **AwsConfig**: AWS S3 client configuration
- **JpaConfig**: JPA/Hibernate configuration
- **AsyncConfig**: Async processing configuration
- **ThreadPoolConfig**: Thread pool configuration
- **AppProperties**: Application properties binding
- **AuditorAwareImpl**: JPA auditing (created/updated by)
- **Filter Chain**: 
  - **RateLimitingFilter**: Rate limiting using Resilience4j
  - **SecurityHeadersFilter**: Security headers (HSTS, XSS, etc.)
  - **RequestIdFilter**: Request ID tracking for logging
  - **RequestLoggingFilter**: Request/response logging
  - **SessionAuthenticationFilter**: Session-based authentication
  - **AuthenticationFilter**: JWT-based authentication
- **GlobalExceptionHandler**: Global exception handling

---

## Code Diagram (Level 4)

### Overview
The Code diagram shows the detailed structure of a key component (Listing Service) as an example.

```
┌─────────────────────────────────────────────────────────────────┐
│                      ListingService Component                   │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Public Methods                        │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │ getAllRecords()                                    │  │   │
│  │  │ - Filters listings                                 │  │   │
│  │  │ - Pagination support                               │  │   │
│  │  │ - Returns PaginatedResponse                        │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │ getRecordsByUser()                                 │  │   │
│  │  │ - User's listings                                  │  │   │
│  │  │ - Favorites support                                │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │ getOne()                                           │  │   │
│  │  │ - Single listing with relations                    │  │   │
│  │  │ - Uses EntityGraph for N+1 prevention              │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │ createRecord()                                     │  │   │
│  │  │ - Creates listing                                  │  │   │
│  │  │ - Uploads images to S3                            │   │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Dependencies                          │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │  Listing     │  │  Listing     │  │   Storage    │    │   │
│  │  │  Repository  │  │  Image      │  │   Service     │    │   │
│  │  │              │  │  Repository  │  │              │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │   Category   │  │   CoreUser    │  │   Location   │   │   │
│  │  │  Repository  │  │   Service    │  │   Service    │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐                                        │   │
│  │  │   Response   │                                        │   │
│  │  │   Mapper      │                                       │   │
│  │  └──────────────┘                                        │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Private Helpers                       │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │ injectPreSignedUrl()                               │  │   │
│  │  │ - Adds S3 presigned URLs to images                 │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Security**: Spring Security (JWT + Session-based)
- **Persistence**: Spring Data JPA (Hibernate)
- **Database**: PostgreSQL 15
- **Caching**: Redis (Spring Cache abstraction)
- **Build**: Maven
- **Serialization**: Jackson (SNAKE_CASE)

### External Services
- **Storage**: AWS S3
- **Email**: SMTP (Gmail)
- **Location API**: External REST API

### Infrastructure
- **Containerization**: Docker / Docker Compose
- **Monitoring**: Prometheus + Grafana
- **Logging**: Logback (JSON in prod, console in dev)

---

## Data Flow Examples

### 1. User Authentication Flow
```
User → AuthController → AuthService → UserRepository → PostgreSQL
                                    ↓
                              SessionRepository → PostgreSQL
                                    ↓
                              Redis (Session Cache)
```

### 2. Listing Creation Flow
```
User → ListingController → ListingService → ListingRepository → PostgreSQL
                                    ↓
                              StorageService → AWS S3
                                    ↓
                              LocationService → LocationRepository → PostgreSQL
```

### 3. Listing Retrieval Flow (with Caching)
```
User → ListingController → ListingService → ListingRepository → PostgreSQL
                                    ↓
                              Redis Cache (if cached)
                                    ↓
                              StorageService → AWS S3 (presigned URLs)
```

---

## Security Architecture

### Authentication
- **JWT Tokens**: Stored in httpOnly cookies
- **Session Management**: Redis-backed sessions
- **OTP Verification**: Email-based OTP for login

### Authorization
- **Role-Based Access Control (RBAC)**: ADMIN, USER roles
- **Method-Level Security**: `@PreAuthorize` annotations
- **Resource-Level Security**: User can only modify own resources

### Security Headers
- HSTS (HTTP Strict Transport Security)
- X-Frame-Options
- X-Content-Type-Options
- X-XSS-Protection
- Content-Security-Policy
- Referrer-Policy

---

## Performance Optimizations

### Caching Strategy
- **Categories**: Cached in Redis (rarely changes)
- **Locations**: Cached by country/state (frequently accessed)
- **Query Results**: Cached for expensive queries

### Database Optimization
- **Indexes**: Strategic indexes on frequently queried columns (see `DATABASE_INDEXES.md`)
- **EntityGraph**: Prevents N+1 query problems
- **Batch Operations**: JDBC batch size configured (20)
- **Projection Queries**: Used for listing lists to avoid loading full entities

### Pagination
- **Offset-based**: For most endpoints
- **Cursor-based**: Recommended for large datasets (see `PERFORMANCE_IMPROVEMENTS.md`)

### Response Compression
- **GZIP compression**: Enabled for JSON, XML, HTML responses
- **Minimum size**: 1KB threshold

---

## Related Documentation

- **Configuration**: See `docs/CONFIGURATION.md` for all configuration properties
- **Database Indexes**: See `docs/DATABASE_INDEXES.md` for index documentation
- **System Architecture**: See `mm-infra/docs/ARCHITECTURE.md` for system-level diagrams

---

## Notes

- This architecture supports horizontal scaling
- Redis is used for both caching and session storage
- Database connections are pooled (HikariCP)
- All external service calls are wrapped with Resilience4j (circuit breakers, retries)
- Rate limiting is applied at the filter level
- Response compression reduces bandwidth usage by 60-80%

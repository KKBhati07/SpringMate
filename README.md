# SpringMate – Backend Service

SpringMate is the **Spring Boot backend** for the **MarketMate** platform (an OLX‑style marketplace for buying and selling used goods). It powers authentication, listings, categories, admin operations, and integrations used by both **Public** and **Admin** front‑end applications.

---

## 🧩 Project Overview

* **Project Name:** SpringMate
* **Role:** Backend / API Layer
* **Architecture:** RESTful APIs
* **Clients:**

    * MarketMate (Public Angular App)
    * MarketMate Admin Portal (Angular App)

SpringMate is designed to be **scalable, cache‑friendly, and production‑ready**, with clear separation of concerns between controllers, services, repositories, and DTOs.

---

## 🛠 Tech Stack

* **Language:** Java 17+
* **Framework:** Spring Boot
* **Security:** Spring Security (Session + JWT based)
* **Persistence:** Spring Data JPA (Hibernate)
* **Database:** PostgreSQL
* **Caching:** Redis (Spring Cache abstraction)
* **Build Tool:** Maven
* **Serialization:** Jackson
* **Containerization:** Docker / Docker Compose

---

## 📁 Project Structure (High Level)

```
SpringMate
├── src/main/java
│   └── com.example.SpringMate
│       ├── Auth            # Authentication & authorization
│       ├── Listing         # Listings, categories, inspections
│       ├── Admin           # Admin-specific APIs
│       ├── Common          # Shared utilities, constants, helpers
│       ├── Config          # Security, Redis, Web, CORS configs
│       ├── Exception       # Global & custom exception handling
│       └── Application.java
├── src/main/resources
│   ├── application.yml
│   └── application-local.yml
└── pom.xml
```

---

## 🔐 Authentication & Authorization

SpringMate supports **shared authentication endpoints** for both Public and Admin apps.

Key points:

* Single login endpoint
* Role-based access (`USER`, `ADMIN`)
* Admin APIs protected using `@PreAuthorize`
* Session persistence supported (DB/Redis)
* **Email + OTP based login flow for users**

### 🔑 Authentication Modes

SpringMate supports multiple authentication mechanisms:

1. **Email + Password Login**

    * Standard credential-based authentication
    * Used by both Public and Admin applications

2. **Email + OTP Login (User Only)**

    * One-time password sent to the user’s registered email
    * Enables password-less authentication for improved UX
    * OTP is time-bound and single-use

### 🛡 Authorization

* Roles are assigned at authentication time
* Access to protected resources is enforced via Spring Security
* Admin-only APIs are guarded using method-level security

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
```

If a non-admin user attempts to access Admin APIs, the backend responds with **401 Unauthorized**.

---

## ⚡ Caching Strategy (Redis)

SpringMate uses **Spring Cache + Redis** for performance‑critical APIs.

### Key Principles

* Cache **DTOs**, not JPA entities
* Use logical cache namespaces
* Avoid large object graphs

Example:

```java
@Cacheable(value = "categories", key = "'all'")
public FetchCategoriesResponseDto getAllCategory() {
    return new FetchCategoriesResponseDto(categoryRepository.findAll());
}
```

### Cache Design Notes

* Redis keys follow the pattern:

  ```
  cacheName::key
  ```
* TTLs are configured via `RedisCacheManager`
* Cache eviction handled explicitly using `@CacheEvict`

---

## 🌐 API Design

* RESTful endpoints
* Versioned APIs (e.g. `/api/v1/...`)
* Consistent response wrapper
* Centralized exception handling

Example response pattern:

```json
{
  "success": true,
  "message": "Categories fetched successfully",
  "data": { ... }
}
```

---

## 🧯 Global Exception Handling

All unhandled exceptions are captured via a **GlobalExceptionHandler**:

* Validation errors
* Authentication / authorization failures
* Business rule violations
* Unexpected runtime errors

This ensures:

* Clean API responses
* No stack traces leaked to clients
* Structured logging for debugging

---

## 🐳 Running Locally (Docker)

### Prerequisites

* Docker
* Docker Compose
* Java 17+

### Run Backend

```bash
docker-compose up --build backend
```

Services typically include:

* SpringMate Backend
* PostgreSQL
* Redis

---

## 🔧 Configuration Profiles

* `application.yml` – common config
* `application-local.yml` – local development
* `application-prod.yml` – production (excluded from VCS)

---

## 📦 Integration with Frontend

SpringMate is consumed by:

* MarketMate Public App
* MarketMate Admin Portal

Key integration features:

* CORS configuration for multi-origin setup
* Cookie-based auth support (`withCredentials`)
* Shared API contracts (DTOs)

---

## 🚀 Production Readiness

✔ Stateless APIs where possible
✔ Redis-backed caching
✔ Role-based security
✔ DTO-based API contracts
✔ Dockerized deployment
✔ Environment-based configuration
✔ Rate limiting  
✔ Security headers  
✔ Input sanitization  
✔ Request size limits

---

## 🔒 Security Features

SpringMate implements multiple layers of security to protect APIs, users, and infrastructure.

---

### CSRF Protection

SpringMate disables Spring Security’s default CSRF protection because the application:

- Is **stateless**
- Does **not use server-side HTTP sessions**
- Uses **token-based authentication**
- Restricts cross-origin access via **CORS**
- Uses **SameSite** and **Secure** cookies for tokens

#### Why CSRF is disabled

Spring Security’s CSRF protection is designed for **stateful, session-based** applications that rely on cookies for authentication.

In SpringMate:

- The server does **not store authentication state**
- Requests are validated using tokens
- Cross-origin requests are restricted
- Cookies cannot be sent cross-site by default

This makes traditional CSRF tokens unnecessary.

---

### Rate Limiting

Rate limiting is implemented using **Resilience4j RateLimiter**:

| Endpoint Type       | Limit                     |
|---------------------|---------------------------|
| Authentication APIs | 10 requests / 60 seconds  |
| Public read APIs    | 200 requests / 60 seconds |
| General APIs        | 100 requests / 60 seconds |
---

### Security Headers

All responses include browser security headers:

- **Strict-Transport-Security (HSTS)** — Forces HTTPS
- **X-Frame-Options** — Prevents clickjacking
- **X-Content-Type-Options** — Prevents MIME sniffing
- **X-XSS-Protection** — Legacy browser XSS filter
- **Referrer-Policy** — Controls referrer leakage
- **Content-Security-Policy (CSP)** — Primary XSS protection
---

### Input Sanitization

User-provided HTML is sanitized using the **OWASP Java HTML Sanitizer (CVE-patched)**:

- Removes scripts and dangerous tags
- Blocks event handlers and unsafe protocols
- Preserves safe formatting
- Prevents XSS without regex-based filtering

Plain text is safely escaped on output.
---

### Request Size Limits

Configured to prevent resource exhaustion:

| Limit            | Value |
|------------------|-------|
| Max file size    | 10MB  |
| Max request size | 50MB  |
| In-memory buffer | 2MB   |

Configured under:

- `spring.servlet.multipart`
- `spring.codec`

---

## 📌 Notes & Best Practices

* Do not cache JPA entities
* Always clear Redis after DTO structure changes
* Prefer constructor injection
* Avoid field injection
* Version APIs explicitly
* Sanitize all user input before processing
* Use parameterized queries for database operations (never concatenate SQL)
* Keep rate limits appropriate for your use case
* Regularly rotate JWT secrets and Prometheus credentials
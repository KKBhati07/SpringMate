# Configuration Overview

This document describes the operational configuration of SpringMate.
It is intended for engineers running or deploying the system.

High-level configuration areas:
- PostgreSQL for persistence
- Redis for caching and sessions
- JWT + session-based authentication
- AWS S3 for file storage
- Resilience4j for rate limiting and fault tolerance

For architectural rationale, see ARCHITECTURE.md.


# Configuration Documentation

This document describes all configuration properties available in SpringMate, their purposes, default values, and environment-specific settings.

---

## Table of Contents

1. [Application Configuration](#application-configuration)
2. [Database Configuration](#database-configuration)
3. [Security Configuration](#security-configuration)
4. [AWS S3 Configuration](#aws-s3-configuration)
5. [Redis Configuration](#redis-configuration)
6. [CORS Configuration](#cors-configuration)
7. [Authentication Configuration](#authentication-configuration)
8. [Resilience4j Configuration](#resilience4j-configuration)
9. [Logging Configuration](#logging-configuration)
10. [Email Configuration](#email-configuration)
11. [Monitoring Configuration](#monitoring-configuration)
12. [Environment-Specific Configuration](#environment-specific-configuration)

---

## Application Configuration

### Basic Settings

```yaml
spring:
  application:
    name: springmate  # Application name (used in logs, metrics)
    seed-secret: ${SEED_SECRET}  # Secret for data seeding endpoints
    location-api-key: ${LOCATION_API_KEY:api-key}  # External location API key
  web:
    resources:
      add-mappings: false  # Disable static resources
  mvc:
    throw-exception-if-no-handler-found: true  # Throw NoHandlerFoundException for unknown routes
  jackson:
    property-naming-strategy: SNAKE_CASE  # JSON property naming (snake_case)
```

**Environment Variables:**
- `SEED_SECRET` - Required for location seeding endpoint
- `LOCATION_API_KEY` - API key for external location service (country/state/city)

**Configuration Properties:**
- `spring.web.resources.add-mappings` - Disables static resource handling (default: `false`)
- `spring.mvc.throw-exception-if-no-handler-found` - Throws exception for unknown routes (default: `true`)
- `spring.jackson.property-naming-strategy` - JSON property naming strategy (default: `SNAKE_CASE`)

---

## Database Configuration

### PostgreSQL Connection

```yaml
spring:
  datasource:
    url: ${DB_URL}  # JDBC connection URL
    username: ${DB_USERNAME}  # Database username
    password: ${DB_PASSWORD}  # Database password
    driver-class-name: org.postgresql.Driver
```

**Environment Variables:**
- `DB_URL` - **Required.** JDBC URL (e.g., `jdbc:postgresql://localhost:5432/springmate`)
- `DB_USERNAME` - **Required.** Database username
- `DB_PASSWORD` - **Required.** Database password

### JPA/Hibernate Settings

**Base Configuration (application.yml):**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Schema update mode (update, validate, none, create-drop)
```

**Local Profile (application-local.yml):**
```yaml
spring:
  jpa:
    properties:
      hibernate:
        format_sql: true   # Pretty SQL for easier debugging in local
        jdbc:
          batch_size: 20  # Batch size for bulk operations
    hibernate:
      ddl-auto: update
```

**Production Profile (application-prod.yml):**
```yaml
spring:
  jpa:
    properties:
      hibernate:
        format_sql: false   # Do not pretty-print SQL; reduces log size and overhead in production
        jdbc:
          batch_size: 20  # Batch size for bulk operations
    hibernate:
      ddl-auto: validate     # NEVER update in prod
```

**Configuration Properties:**
- `format_sql` - Format SQL queries for readability (default: `false` in base, `true` in local, `false` in prod)
- `jdbc.batch_size` - Batch size for bulk operations (default: `20`)

**Note:** 
- For production, `ddl-auto` is set to `validate` to prevent accidental schema changes
- For local development, `ddl-auto` is set to `update` for convenience

**Query Performance Logging:**
Query performance logging is configured via `logback-spring.xml` and profile-specific logging levels:

**Local Profile:**
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG          # Logs generated SQL statements
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # Logs query parameter values
```

**Note:** The actual Hibernate parameter binding logger is `org.hibernate.type.descriptor.sql.BasicBinder` (configured in `logback-spring.xml`), not `org.hibernate.orm.jdbc.bind`.

**Production Profile:**
```yaml
logging:
  level:
    org.hibernate.SQL: INFO
    org.hibernate.type.descriptor.sql.BasicBinder: OFF  # Disabled to avoid sensitive data leaks
```

**Note:** The actual Hibernate parameter binding logger is `org.hibernate.type.descriptor.sql.BasicBinder` (configured in `logback-spring.xml`), not `org.hibernate.orm.jdbc.bind`.

Enable via logback configuration or Spring Boot Actuator loggers endpoint.

---

## Security Configuration

### Security Headers

```yaml
app:
  security:
    headers:
      hsts:
        enabled: ${SECURITY_HSTS_ENABLED:true}
        max-age-seconds: ${SECURITY_HSTS_MAX_AGE:3600}
        include-subdomains: ${SECURITY_HSTS_INCLUDE_SUBDOMAINS:true}
      frame-options: ${SECURITY_FRAME_OPTIONS:DENY}
      content-type-options: ${SECURITY_CONTENT_TYPE_OPTIONS:true}
      xss-protection: ${SECURITY_XSS_PROTECTION:true}
      referrer-policy: ${SECURITY_REFERRER_POLICY:strict-origin-when-cross-origin}
      csp: "default-src 'self'; frame-ancestors 'none';"
```

**Environment Variables:**
- `SECURITY_HSTS_ENABLED` - Enable HSTS header (default: `true`)
- `SECURITY_HSTS_MAX_AGE` - HSTS max-age in seconds (default: `3600`)
- `SECURITY_HSTS_INCLUDE_SUBDOMAINS` - Include subdomains in HSTS (default: `true`)
- `SECURITY_FRAME_OPTIONS` - X-Frame-Options value: `DENY`, `SAMEORIGIN`, or `ALLOW-FROM` (default: `DENY`)
- `SECURITY_CONTENT_TYPE_OPTIONS` - Enable X-Content-Type-Options (default: `true`)
- `SECURITY_XSS_PROTECTION` - Enable X-XSS-Protection header (legacy, CSP is primary protection) (default: `true`)
- `SECURITY_REFERRER_POLICY` - Referrer-Policy value (default: `strict-origin-when-cross-origin`)

### Request Size Limits

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB  # Maximum file size per upload
      max-request-size: 50MB  # Maximum total request size
  codec:
    max-in-memory-size: 2MB  # Maximum in-memory buffer size
```

**Purpose:** Prevents DoS attacks via large file uploads.

---

## AWS S3 Configuration

```yaml
app:
  aws:
    region: ${AWS_REGION:ap-south-1}
    bucket-name: ${AWS_BUCKET_NAME:marketmatestore}
    presign:
      get-expiry-minutes: ${AWS_GET_URL_EXPIRY:10}
      put-expiry-minutes: ${AWS_PUT_URL_EXPIRY:15}
```

**Environment Variables:**
- `AWS_REGION` - AWS region (default: `ap-south-1`)
- `AWS_BUCKET_NAME` - S3 bucket name (default: `marketmatestore`)
- `AWS_GET_URL_EXPIRY` - Presigned GET URL expiry in minutes (default: `10`)
- `AWS_PUT_URL_EXPIRY` - Presigned PUT URL expiry in minutes (default: `15`)

**AWS Credentials:**
- Configured via AWS SDK default credential chain:
  - Environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
  - IAM roles (for EC2/ECS)
  - AWS credentials file (`~/.aws/credentials`)

---

## Redis Configuration

Redis is used for caching and session storage in the application.

### Local Development Configuration

```yaml
spring:
  data:
    redis:
      host: redis          # Redis server hostname
      port: 6379           # Redis server port
      database: 0          # Redis database index (0-15)
      timeout: 60s         # Connection timeout
      lettuce:
        pool:
          max-active: 8    # Maximum active connections
          max-idle: 8      # Maximum idle connections
```

**Configuration Properties:**
- `host` - Redis server hostname (default: `redis` in local profile)
- `port` - Redis server port (default: `6379`)
- `database` - Redis database index (default: `0`)
- `timeout` - Connection timeout (default: `60s`)
- `lettuce.pool.max-active` - Maximum active connections (default: `8`)
- `lettuce.pool.max-idle` - Maximum idle connections (default: `8`)

**Redis Serialization:**
- Keys: String serializer
- Values: JSON serializer (GenericJackson2JsonRedisSerializer)
- Cache TTL: 30 minutes (default)

**Note:** Redis configuration is only present in `application-local.yml`. For production, configure Redis connection via environment variables or production profile.

---

## CORS Configuration

```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200}
    allowed-methods:
      - GET
      - POST
      - PUT
      - PATCH
      - DELETE
      - OPTIONS
    allowed-headers:
      - "*"
    allow-credentials: true
    max-age-seconds: 3600
```

**Environment Variables:**
- `CORS_ALLOWED_ORIGINS` - Comma-separated list of allowed origins (default: `http://localhost:4200`)

**Example for multiple origins:**
```yaml
CORS_ALLOWED_ORIGINS=http://localhost:4200,https://app.example.com,https://admin.example.com
```

---

## Authentication Configuration

### JWT Settings

```yaml
app:
  auth:
    jwt:
      secret: ${JWT_SECRET}  # Required
      validity-days: ${JWT_VALIDITY_DAYS:7}
```

**Environment Variables:**
- `JWT_SECRET` - **Required.** Secret key for JWT signing (use strong random string)
- `JWT_VALIDITY_DAYS` - JWT token validity in days (default: `7`)

### Session Settings

```yaml
app:
  auth:
    session:
      validity-days: ${SESSION_VALIDITY_DAYS:7}
```

**Environment Variables:**
- `SESSION_VALIDITY_DAYS` - Session validity in days (default: `7`)

### OTP Settings

```yaml
app:
  auth:
    otp:
      expiration-minutes: ${OTP_EXPIRATION_MINUTES:10}
```

**Environment Variables:**
- `OTP_EXPIRATION_MINUTES` - OTP expiration time in minutes (default: `10`)

### Cookie Settings

```yaml
app:
  cookie:
    domain: ${COOKIE_DOMAIN:.localhost}
    secure: ${COOKIE_SECURE:true}
    path: /
```

**Environment Variables:**
- `COOKIE_DOMAIN` - Cookie domain (default: `.localhost`)
- `COOKIE_SECURE` - Secure flag (HTTPS only, default: `true`)

---

## Resilience4j Configuration

### Rate Limiting

```yaml
resilience4j:
  ratelimiter:
    instances:
      api:
        limit-for-period: 100              # Number of requests allowed per period
        limit-refresh-period: 60s          # Time window for rate limit (60 seconds)
        timeout-duration: 0s               # Wait time for permit (0 = fail immediately)
        register-health-indicator: true     # Exposes rate limiter metrics to actuator
      auth:
        limit-for-period: 10               # Stricter limit for authentication endpoints
        limit-refresh-period: 60s
        timeout-duration: 0s
        register-health-indicator: true
      public:
        limit-for-period: 200               # More lenient for public read endpoints
        limit-refresh-period: 60s
        timeout-duration: 0s
        register-health-indicator: true
```

**Rate Limits:**
- **api:** 100 requests per 60 seconds (general endpoints)
- **auth:** 10 requests per 60 seconds (authentication endpoints)
- **public:** 200 requests per 60 seconds (public read endpoints)

### Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
    instances:
      s3Upload:
        register-health-indicator: true
        sliding-window-size: 5
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
      sendEmail:
        register-health-indicator: true
        sliding-window-size: 5
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

**Circuit Breakers:**
- **s3Upload:** Protects AWS S3 upload operations
- **sendEmail:** Protects email sending operations

### Retry

```yaml
resilience4j:
  retry:
    instances:
      s3Upload:
        max-attempts: 3               # Number of retry attempts (1 initial + 2 retries)
        wait-duration: 1s             # Wait duration between retry attempts
      sendEmail:
        max-attempts: 3               # Number of retry attempts (1 initial + 2 retries)
        wait-duration: 1s             # Wait duration between retry attempts
```

**Retry Configuration:**
- Both `s3Upload` and `sendEmail` retry up to 3 times with 1 second wait between attempts

---

## Logging Configuration

### Logback Configuration

Logging is configured via `logback-spring.xml`:

- **Local/Dev:** Human-readable console output
- **Production:** JSON structured logging

### Log Levels

Configure via Spring Boot Actuator:
```bash
curl -X POST http://localhost:8080/actuator/loggers/com.example.SpringMate \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

Or via `application.yml`:
```yaml
logging:
  level:
    com.example.SpringMate: DEBUG
    org.springframework.web: INFO
    org.springframework.security: INFO
```

---

## Email Configuration

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
        debug: true
```

**Environment Variables:**
- `EMAIL_USERNAME` - **Required.** SMTP username
- `EMAIL_PASSWORD` - **Required.** SMTP password or app password

---

## Monitoring Configuration

### Spring Boot Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,loggers,httptrace
        exclude: shutdown,heapdump,threaddump
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
      probes:
        enabled: true
    info:
      enabled: true
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

**Available Endpoints:**
- `/actuator/health` - Health check endpoint
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics (HTTP Basic Auth required)
- `/actuator/env` - Environment properties
- `/actuator/loggers` - Logger configuration
- `/actuator/httptrace` - HTTP request traces

**Excluded Endpoints (for security):**
- `/actuator/shutdown` - Application shutdown
- `/actuator/heapdump` - Heap dump
- `/actuator/threaddump` - Thread dump

### Prometheus

```yaml
app:
  monitoring:
    prometheus:
      username: prometheus
      password: ${PROMETHEUS_PASS:prometheus-pass}
```

**Environment Variables:**
- `PROMETHEUS_PASS` - Prometheus endpoint password (default: `prometheus-pass`)

**Access:** `http://localhost:8080/actuator/prometheus` (HTTP Basic Auth)

---

## Environment-Specific Configuration

### Local Development (application-local.yml)

**Database Connection Pool:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1
```

**Redis Configuration:**
```yaml
spring:
  data:
    redis:
      host: redis          # Redis server hostname
      port: 6379           # Redis server port
      database: 0          # Redis database index (0-15)
      timeout: 60s         # Connection timeout
      lettuce:
        pool:
          max-active: 8    # Maximum active connections
          max-idle: 8      # Maximum idle connections
```

**Purpose:** Redis is used for caching and session storage.

**Configuration Properties:**
- `host` - Redis server hostname (default: `redis` in local profile)
- `port` - Redis server port (default: `6379`)
- `database` - Redis database index (default: `0`)
- `timeout` - Connection timeout (default: `60s`)
- `lettuce.pool.max-active` - Maximum active connections (default: `8`)
- `lettuce.pool.max-idle` - Maximum idle connections (default: `8`)

**Note:** Redis configuration is only present in `application-local.yml`. For production, configure Redis connection via environment variables or production profile.

**JPA/Hibernate:**
- `format_sql: true` - Pretty SQL for debugging
- `ddl-auto: update` - Auto-update schema
- `jdbc.batch_size: 20`

**Server Configuration:**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:marketmate-local.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD:changeit}
    key-store-type: PKCS12
    key-alias: marketmate
  tomcat:
    threads:
      max: 30
      min-spare: 5
    max-connections: 100
    accept-count: 20
    backlog: 20
```

**Logging Levels:**
```yaml
logging:
  level:
    org.springframework.boot.devtools: DEBUG
    org.springframework.data.redis: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**Note:** The actual Hibernate parameter binding logger is `org.hibernate.type.descriptor.sql.BasicBinder` (configured in `logback-spring.xml`), not `org.hibernate.orm.jdbc.bind`.

**CORS & Cookies:**
```yaml
app:
  cors:
    allowed-origins:
      - https://marketmate.local:4200
      - https://admin.marketmate.local:4300
  cookie:
    domain: .marketmate.local
    secure: true  # Using HTTPS locally with self-signed cert
```

**Environment Variables:**
- `SSL_KEYSTORE_PASSWORD` - SSL keystore password (default: `changeit`)

---

### Production (application-prod.yml)

**Database Connection Pool:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
```

**JPA/Hibernate:**
- `format_sql: false` - Reduces log size and overhead
- `ddl-auto: validate` - **NEVER update in prod** - Prevents accidental schema changes
- `jdbc.batch_size: 20`

**Server Configuration:**
```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 20
    max-connections: 500
    accept-count: 100
    backlog: 100
```

**Logging Levels:**
```yaml
logging:
  level:
    org.hibernate.SQL: INFO
    org.hibernate.type.descriptor.sql.BasicBinder: OFF  # Disabled to avoid sensitive data leaks
```

**Note:** The actual Hibernate parameter binding logger is `org.hibernate.type.descriptor.sql.BasicBinder` (configured in `logback-spring.xml`), not `org.hibernate.orm.jdbc.bind`.

**CORS, AWS & Cookies:**
```yaml
app:
  aws:
    region: ${AWS_REGION}
    bucket-name: ${AWS_BUCKET_NAME}
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS}
  cookie:
    domain: ${COOKIE_DOMAIN}
    secure: true
```

**Environment Variables (Required in Production):**
- `AWS_REGION` - AWS region for S3
- `AWS_BUCKET_NAME` - S3 bucket name
- `CORS_ALLOWED_ORIGINS` - Comma-separated list of allowed origins
- `COOKIE_DOMAIN` - Cookie domain for production

**Profile Activation:**
```bash
# Local
java -jar app.jar --spring.profiles.active=local

# Production
java -jar app.jar --spring.profiles.active=prod
```

**Note:** `application-prod.yml` should be excluded from version control and managed via deployment configuration or secret management.

---

## Configuration Validation

All configuration properties are validated on startup:

- **Required fields** will cause startup failure if missing
- **Validation annotations** ensure values are within acceptable ranges
- **Type checking** ensures correct data types

**Validation Errors:**
If configuration validation fails, the application will not start. Check logs for specific validation errors.

---

## Best Practices

1. **Never commit secrets:** Use environment variables or secret management
2. **Use profiles:** Separate configuration for different environments
3. **Validate early:** Fix configuration issues before deployment
4. **Document changes:** Update this document when adding new properties
5. **Use defaults wisely:** Provide sensible defaults for optional properties

---

## Troubleshooting

### Configuration Not Loading

- Check environment variable names (case-sensitive)
- Verify profile is active: `--spring.profiles.active=local`
- Check for typos in property names

### Validation Errors

- Review startup logs for specific validation messages
- Ensure required environment variables are set
- Check value ranges (e.g., `@Min` constraints)

### Profile-Specific Issues

- Verify correct profile is active
- Check `application-{profile}.yml` exists
- Ensure profile-specific properties override base config correctly

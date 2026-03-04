-- SpringMate schema for PostgreSQL
-- Generated from JPA entities. Run against an empty database (e.g. after CREATE DATABASE).

-- =============================================================================
-- 1. roles
-- =============================================================================
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- =============================================================================
-- 2. countries
-- =============================================================================
CREATE TABLE countries (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    iso2 VARCHAR(255) NOT NULL UNIQUE
);

-- =============================================================================
-- 3. states
-- =============================================================================
CREATE TABLE states (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255),
    iso2       VARCHAR(255) NOT NULL,
    country_id BIGINT NOT NULL REFERENCES countries(id)
);
CREATE UNIQUE INDEX unique_states ON states (name, country_id);

-- =============================================================================
-- 4. cities
-- =============================================================================
CREATE TABLE cities (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(255),
    state_id  BIGINT NOT NULL REFERENCES states(id)
);
CREATE UNIQUE INDEX unique_cities ON cities (name, state_id);

-- =============================================================================
-- 5. locations
-- =============================================================================
CREATE TABLE locations (
    id         BIGSERIAL PRIMARY KEY,
    city_id    BIGINT NOT NULL REFERENCES cities(id),
    state_id   BIGINT NOT NULL REFERENCES states(id),
    country_id BIGINT NOT NULL REFERENCES countries(id)
);
CREATE UNIQUE INDEX unique_location ON locations (city_id, state_id, country_id);

-- =============================================================================
-- 6. users
-- =============================================================================
CREATE TABLE users (
    id             BIGSERIAL PRIMARY KEY,
    uuid           UUID NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    password       VARCHAR(255),
    email          VARCHAR(255) NOT NULL UNIQUE,
    role_id        BIGINT NOT NULL REFERENCES roles(id),
    profile_url    VARCHAR(255),
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    contact_no     VARCHAR(255),
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    updated_by     BIGINT REFERENCES users(id),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE
);

-- =============================================================================
-- 7. categories
-- =============================================================================
CREATE TABLE categories (
    id     BIGSERIAL PRIMARY KEY,
    icon   VARCHAR(255),
    name   VARCHAR(255),
    active BOOLEAN DEFAULT TRUE
);

-- =============================================================================
-- 8. listing_conditions
-- =============================================================================
CREATE TABLE listing_conditions (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50) NOT NULL UNIQUE,
    label       VARCHAR(100) NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    description VARCHAR(500),
    active      BOOLEAN DEFAULT TRUE
);

-- =============================================================================
-- 9. listings
-- =============================================================================
CREATE TABLE listings (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    description   VARCHAR(1000),
    price         DOUBLE PRECISION NOT NULL,
    is_sold       BOOLEAN DEFAULT FALSE,
    posted_at     TIMESTAMP,
    updated_at    TIMESTAMP,
    is_deleted    BOOLEAN DEFAULT FALSE,
    category_id   BIGINT REFERENCES categories(id),
    seller_id     BIGINT REFERENCES users(id),
    updated_by_id BIGINT REFERENCES users(id),
    location_id   BIGINT REFERENCES locations(id),
    condition_id  BIGINT REFERENCES listing_conditions(id)
);

-- =============================================================================
-- 10. listing_images
-- =============================================================================
CREATE TABLE listing_images (
    id         BIGSERIAL PRIMARY KEY,
    url        VARCHAR(255) NOT NULL,
    is_cover   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    listing_id BIGINT REFERENCES listings(id)
);

-- =============================================================================
-- 11. contact_messages
-- =============================================================================
CREATE TABLE contact_messages (
    id             BIGSERIAL PRIMARY KEY,
    listing_id     BIGINT NOT NULL REFERENCES listings(id),
    seller_id      BIGINT NOT NULL REFERENCES users(id),
    buyer_id       BIGINT NOT NULL REFERENCES users(id),
    status         VARCHAR(20) NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    sent_at        TIMESTAMP,
    failure_reason VARCHAR(500),
    subject_length INTEGER,
    body_length    INTEGER
);
CREATE INDEX idx_contact_messages_listing_id ON contact_messages (listing_id);
CREATE INDEX idx_contact_messages_seller_id ON contact_messages (seller_id);
CREATE INDEX idx_contact_messages_buyer_id ON contact_messages (buyer_id);
CREATE INDEX idx_contact_messages_status ON contact_messages (status);

-- =============================================================================
-- 12. user_favorites
-- =============================================================================
CREATE TABLE user_favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    listing_id  BIGINT NOT NULL REFERENCES listings(id),
    is_favorite BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    UNIQUE (user_id, listing_id)
);

-- =============================================================================
-- 13. sessions
-- =============================================================================
CREATE TABLE sessions (
    id              BIGSERIAL PRIMARY KEY,
    session_id      VARCHAR(255) NOT NULL,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL,
    last_accessed_at TIMESTAMP NOT NULL,
    expires_at      TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX idx_sessions_session_id ON sessions (session_id);

-- =============================================================================
-- 14. session_logs
-- =============================================================================
CREATE TABLE session_logs (
    id         BIGSERIAL PRIMARY KEY,
    login_at   TIMESTAMP NOT NULL,
    logout_at  TIMESTAMP,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    session_id VARCHAR(255) NOT NULL UNIQUE,
    user_id    BIGINT NOT NULL REFERENCES users(id)
);

-- =============================================================================
-- 15. verification_codes
-- =============================================================================
CREATE TABLE verification_codes (
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(255) NOT NULL,
    type       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    user_id    BIGINT NOT NULL REFERENCES users(id)
);

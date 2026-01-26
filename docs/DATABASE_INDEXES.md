# Database Indexes Documentation

This document describes all database indexes, unique constraints, and their purposes in the SpringMate application.

## Overview

Indexes are critical for query performance. This document tracks all indexes defined in JPA entities to ensure optimal database performance.

---

## Indexes by Table

### `users` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Unique Constraints:**
- `uuid` (UUID) - Unique identifier for user lookup
- `email` (VARCHAR) - Unique email address for authentication

**Indexes:**
- **Implicit indexes** (created by foreign keys):
  - `role_id` - Foreign key to `roles` table
  - `updated_by` - Foreign key to `users` table (self-reference)

**Query Patterns:**
- Lookup by `email` (authentication) - ✅ Indexed via unique constraint
- Lookup by `uuid` (API calls) - ✅ Indexed via unique constraint
- Lookup by `email AND deleted = false` - ⚠️ Consider composite index `(email, deleted)`
- Lookup by `uuid AND deleted = false` - ⚠️ Consider composite index `(uuid, deleted)`
- Filter by `role_id` - ✅ Indexed via foreign key
- Filter by `is_deleted = false` - ⚠️ Consider adding index (frequently used in queries)

**Recommended Additional Indexes:**
- `is_deleted` - **High priority** - Used in most user queries (`findByEmailAndDeletedFalse`, `findByUuidAndDeletedFalse`)
- `(email, deleted)` - Composite index for common query pattern
- `(uuid, deleted)` - Composite index for common query pattern
- `created_at` - For sorting/filtering by registration date (if needed)

---

### `sessions` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Indexes:**
- `idx_sessions_session_id` (UNIQUE) - On `session_id` column
  - **Purpose:** Fast lookup of sessions by session ID
  - **Usage:** Session validation, authentication checks
  - **Type:** Unique index

**Implicit Indexes:**
- `user_id` - Foreign key to `users` table

**Query Patterns:**
- Lookup by `session_id` - ✅ Indexed
- Lookup by `user_id` - ✅ Indexed via foreign key
- Filter by `expires_at` - ⚠️ Consider adding index for cleanup queries

**Recommended Additional Indexes:**
- `expires_at` - For session cleanup/expiration queries
- `(user_id, expires_at)` - Composite index for user session queries

---

### `session_logs` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Unique Constraints:**
- `session_id` (VARCHAR) - Unique session identifier
  - **Purpose:** Fast lookup of session logs by session ID
  - **Type:** Unique constraint (creates unique index)

**Implicit Indexes:**
- `user_id` - Foreign key to `users` table

**Query Patterns:**
- Lookup by `session_id` - ✅ Indexed via unique constraint
- Filter by `user_id` - ✅ Indexed via foreign key

---

### `listings` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Implicit Indexes:**
- `seller_id` - Foreign key to `users` table
- `category_id` - Foreign key to `categories` table (via `category` relationship)
- `location_id` - Foreign key to `locations` table
- `updated_by_id` - Foreign key to `users` table

**Query Patterns:**
- Filter by `seller_id` - ✅ Indexed via foreign key
- Filter by `category_id AND deleted = false` - ✅ Indexed via foreign key + ⚠️ Consider composite index
- Filter by `location_id` - ✅ Indexed via foreign key
- Filter by `deleted = false` - ⚠️ **High priority** - Used in all listing queries
- Filter by `seller_id AND deleted = false` - ✅ Foreign key + ⚠️ Consider composite index
- Sort by `posted_at DESC` - ⚠️ **High priority** - Used in listing pagination
- Filter by price range (`price >= minPrice AND price <= maxPrice`) - ⚠️ Consider index
- Full-text search on `title` and `description` - ⚠️ Consider full-text index
- Complex filtering: `deleted`, `category_id`, `location` (country/state/city), `price`, `searchString` - ⚠️ Multiple indexes needed

**Recommended Additional Indexes:**
- `is_deleted` - **High priority** - Used in all listing queries
- `posted_at` - **High priority** - Used for sorting in pagination (DESC order)
- `(category_id, deleted, posted_at)` - Composite index for category browsing with pagination
- `(seller_id, deleted, posted_at)` - Composite index for user's listings
- `price` - For price range filtering (if frequently used)
- `(deleted, posted_at)` - Composite index for general listing queries
- Full-text index on `(title, description)` - For search functionality (PostgreSQL `tsvector`)

**Note:** The application uses complex queries with multiple filters. Consider analyzing query execution plans to optimize index selection.

---

### `user_favorites` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Unique Constraints:**
- `(user_id, listing_id)` - Composite unique constraint
  - **Purpose:** Prevent duplicate favorites
  - **Type:** Unique constraint (creates unique index)

**Implicit Indexes:**
- `user_id` - Foreign key to `users` table
- `listing_id` - Foreign key to `listings` table

**Query Patterns:**
- Lookup by `(user_id, listing_id)` - ✅ Indexed via unique constraint
- Filter by `user_id AND listing` - ✅ Indexed via unique constraint
- Filter by `user_id AND isFavorite = true` - ⚠️ **High priority** - Used in favorites queries
- Filter by `listing_id` - ✅ Indexed via foreign key

**Recommended Additional Indexes:**
- `(user_id, is_favorite)` - **High priority** - Composite index for user's favorite listings query

---

### `locations` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Unique Constraints:**
- `Unique_Location` - Composite unique constraint on `(city_id, state_id, country_id)`
  - **Purpose:** Ensure unique location combinations
  - **Type:** Unique constraint (creates unique index)

**Implicit Indexes:**
- `city_id` - Foreign key to `cities` table
- `state_id` - Foreign key to `states` table
- `country_id` - Foreign key to `countries` table

**Query Patterns:**
- Lookup by `(city_id, state_id, country_id)` - ✅ Indexed via unique constraint
- Filter by `country_id` - ✅ Indexed via foreign key
- Filter by `state_id` - ✅ Indexed via foreign key
- Filter by `city_id` - ✅ Indexed via foreign key

---

### `cities` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Unique Constraints:**
- `Unique_cities` - Composite unique constraint on `(name, state_id)`
  - **Purpose:** Ensure unique city names within a state
  - **Type:** Unique constraint (creates unique index)

**Implicit Indexes:**
- `state_id` - Foreign key to `states` table

**Query Patterns:**
- Lookup by `(name, state_id)` - ✅ Indexed via unique constraint
- Filter by `state_id` - ✅ Indexed via foreign key

---

### `states` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Unique Constraints:**
- `Unique_states` - Composite unique constraint on `(name, country_id)`
  - **Purpose:** Ensure unique state names within a country
  - **Type:** Unique constraint (creates unique index)

**Implicit Indexes:**
- `country_id` - Foreign key to `countries` table

**Query Patterns:**
- Lookup by `(name, country_id)` - ✅ Indexed via unique constraint
- Filter by `country_id` - ✅ Indexed via foreign key

---

### `countries` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Unique Constraints:**
- `iso2` (VARCHAR) - Unique ISO 2-letter country code
  - **Purpose:** Fast lookup by country code
  - **Type:** Unique constraint (creates unique index)

**Query Patterns:**
- Lookup by `iso2` - ✅ Indexed via unique constraint
- Lookup by `name` (case-insensitive) - ⚠️ Consider adding index if frequently used
- Filter by `iso2 IN (...)` - ✅ Indexed via unique constraint

**Recommended Additional Indexes:**
- `name` - For case-insensitive name lookups (if frequently used)

---

### `roles` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Query Patterns:**
- Lookup by `id` - ✅ Indexed via primary key
- Lookup by `name` - ⚠️ Consider adding unique index if names are unique (frequently used in authentication)

**Recommended Additional Indexes:**
- `name` - **High priority** - Used in role lookups (`findByName`) for user authentication

---

### `categories` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Query Patterns:**
- Lookup by `id` - ✅ Indexed via primary key
- Filter by `active = true` - ⚠️ Consider adding index if frequently used

**Recommended Additional Indexes:**
- `name` - For category name lookups (if frequently used)
- `active` - For filtering active categories (if frequently used)

---

### `listing_images` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Implicit Indexes:**
- `listing_id` - Foreign key to `listings` table

**Query Patterns:**
- Filter by `listing_id` - ✅ Indexed via foreign key
- Filter by `listing_id AND isCover = true ORDER BY createdAt ASC` - ⚠️ Consider composite index

**Recommended Additional Indexes:**
- `(listing_id, is_cover, created_at)` - Composite index for cover image queries

---

### `verification_codes` Table

**Primary Key:**
- `id` (BIGINT, AUTO_INCREMENT)

**Implicit Indexes:**
- `user_id` - Foreign key to `users` table

**Query Patterns:**
- Lookup by `user AND type ORDER BY createdAt DESC` - ⚠️ **High priority** - Used in `findTopByUserAndTypeOrderByCreatedAtDesc`
- Delete by `user AND type` - Used in `deleteByUserAndType`
- Filter by `expires_at` - ⚠️ Consider adding index for cleanup

**Recommended Additional Indexes:**
- `(user_id, type, created_at)` - **High priority** - Composite index for OTP lookup query
- `expires_at` - For cleanup of expired codes (if cleanup job exists)

---

## Index Maintenance

### Monitoring

- Use PostgreSQL's `pg_stat_user_indexes` to monitor index usage
- Review slow query logs to identify missing indexes
- Use `EXPLAIN ANALYZE` to verify index usage in queries

### Best Practices

1. **Don't over-index:** Each index adds write overhead
2. **Monitor unused indexes:** Remove indexes that aren't being used
3. **Consider composite indexes:** For queries filtering multiple columns
4. **Update statistics:** Run `ANALYZE` regularly for query planner

### Adding New Indexes

When adding new indexes:

1. Document the index purpose and query pattern
2. Test performance impact (both reads and writes)
3. Update this documentation
4. Consider using `CREATE INDEX CONCURRENTLY` in production

---

## Performance Considerations

### High-Traffic Queries

These queries should be optimized with appropriate indexes:

1. **User authentication:** `users.email` - ✅ Indexed via unique constraint
2. **User lookup:** `users.uuid` - ✅ Indexed via unique constraint
3. **User queries with soft-delete:** `users.email AND deleted = false` - ⚠️ Needs composite index
4. **Session validation:** `sessions.session_id` - ✅ Indexed via unique index
5. **Listing browsing:** `listings.category_id`, `listings.location_id` - ✅ Indexed via foreign keys
6. **Listing pagination:** `listings.deleted = false ORDER BY posted_at DESC` - ⚠️ Needs composite index
7. **User favorites:** `user_favorites.user_id AND is_favorite = true` - ⚠️ Needs composite index
8. **OTP verification:** `verification_codes.user_id AND type ORDER BY created_at DESC` - ⚠️ Needs composite index
9. **Listing search:** Complex queries with filters on `deleted`, `category_id`, `location`, `price`, `title/description` - ⚠️ Multiple indexes needed

### Potential Bottlenecks

Monitor these areas for potential index additions:

1. Listing search/filter operations
2. User listing queries (by seller_id)
3. Session expiration cleanup
4. Verification code cleanup

---

## Notes

- All foreign key columns automatically create indexes in PostgreSQL
- Unique constraints automatically create unique indexes
- This documentation should be updated when new indexes are added
- Review index usage quarterly to identify optimization opportunities

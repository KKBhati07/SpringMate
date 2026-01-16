package com.example.SpringMate.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Centralized application configuration properties.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    private final Aws aws = new Aws();
    private final Cors cors = new Cors();
    private final Cookie cookie = new Cookie();
    private final Auth auth = new Auth();

    @Data
    public static class Aws {
        /**
         * AWS region for S3 operations
         */
        @NotBlank(message = "AWS region is required")
        private String region;

        /**
         * S3 bucket name for file storage
         */
        @NotBlank(message = "AWS bucket name is required")
        private String bucketName;

        private final Presign presign = new Presign();

        @Data
        public static class Presign {
            /**
             * Expiration time in minutes for GET presigned URLs
             */
            @Min(value = 1, message = "GET URL expiry must be at least 1 minute")
            private int getExpiryMinutes = 10;

            /**
             * Expiration time in minutes for PUT presigned URLs
             */
            @Min(value = 1, message = "PUT URL expiry must be at least 1 minute")
            private int putExpiryMinutes = 15;
        }
    }

    @Data
    public static class Cors {
        /**
         * List of allowed origins for CORS configuration
         */
        @NotEmpty(message = "At least one allowed origin is required")
        private List<String> allowedOrigins;

        /**
         * List of allowed HTTP methods
         */
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        /**
         * List of allowed headers
         */
        private List<String> allowedHeaders = List.of("*");

        /**
         * Whether to allow credentials (cookies, authorization headers)
         */
        private boolean allowCredentials = true;

        /**
         * Max age for preflight cache in seconds
         */
        private long maxAgeSeconds = 3600L;
    }

    @Data
    public static class Cookie {
        /**
         * Domain for authentication cookies
         */
        @NotBlank(message = "Cookie domain is required")
        private String domain;

        /**
         * Whether cookies should be secure (HTTPS only)
         */
        private boolean secure = true;

        /**
         * Cookie path
         */
        private String path = "/";
    }

    @Data
    public static class Auth {
        private final Jwt jwt = new Jwt();
        private final Session session = new Session();
        private final Otp otp = new Otp();

        @Data
        public static class Jwt {
            /**
             * JWT secret key for signing tokens
             */
            @NotBlank(message = "JWT secret is required")
            private String secret;

            /**
             * JWT token validity in days
             */
            @Min(value = 1, message = "JWT validity must be at least 1 day")
            private int validityDays = 7;
        }

        @Data
        public static class Session {
            /**
             * Session validity in days
             */
            @Min(value = 1, message = "Session validity must be at least 1 day")
            private int validityDays = 7;
        }

        @Data
        public static class Otp {
            /**
             * OTP expiration time in minutes
             */
            @Min(value = 1, message = "OTP expiration must be at least 1 minute")
            private int expirationMinutes = 10;
        }
    }
}

package com.example.SpringMate.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    private final Aws aws = new Aws();
    private final Cors cors = new Cors();
    private final Cookie cookie = new Cookie();
    private final Auth auth = new Auth();
    private final Security security = new Security();

    @Data
    public static class Aws {
        @NotBlank(message = "AWS region is required")
        private String region;

        @NotBlank(message = "AWS bucket name is required")
        private String bucketName;

        private final Presign presign = new Presign();

        @Data
        public static class Presign {
            @Min(value = 1, message = "GET URL expiry must be at least 1 minute")
            private int getExpiryMinutes = 10;

            @Min(value = 1, message = "PUT URL expiry must be at least 1 minute")
            private int putExpiryMinutes = 15;
        }
    }

    @Data
    public static class Cors {
        @NotEmpty(message = "At least one allowed origin is required")
        private List<String> allowedOrigins;

        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        private List<String> allowedHeaders = List.of("*");

        private boolean allowCredentials = true;

        private long maxAgeSeconds = 3600L;
    }

    @Data
    public static class Cookie {
        @NotBlank(message = "Cookie domain is required")
        private String domain;

        private boolean secure = true;

        private String path = "/";
    }

    @Data
    public static class Auth {
        private final Jwt jwt = new Jwt();
        private final Session session = new Session();
        private final Otp otp = new Otp();

        @Data
        public static class Jwt {
            @NotBlank(message = "JWT secret is required")
            private String secret;

            @Min(value = 1, message = "JWT validity must be at least 1 day")
            private int validityDays = 7;
        }

        @Data
        public static class Session {
            @Min(value = 1, message = "Session validity must be at least 1 day")
            private int validityDays = 7;
        }

        @Data
        public static class Otp {
            @Min(value = 1, message = "OTP expiration must be at least 1 minute")
            private int expirationMinutes = 10;
        }
    }

    @Data
    public static class Security {
        private final Headers headers = new Headers();

        @Data
        public static class Headers {
            private final Hsts hsts = new Hsts();
            
            @NotBlank(message = "Frame options must be specified")
            private String frameOptions = "DENY";
            
            private boolean contentTypeOptions = true;
            private boolean xssProtection = true;
            
            @NotBlank(message = "Referrer policy must be specified")
            private String referrerPolicy = "strict-origin-when-cross-origin";
            
            private String csp = "default-src 'self'; frame-ancestors 'none';";

            @Data
            public static class Hsts {
                private boolean enabled = true;
                
                @Min(value = 0, message = "HSTS max age must be non-negative")
                private long maxAgeSeconds = 3600L;
                
                private boolean includeSubdomains = true;
            }
        }
    }
}

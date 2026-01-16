package com.example.SpringMate.Config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Static holder for application configuration values.
 * <p>
 * This class provides static access to configuration properties for cases where
 * dependency injection is not possible (e.g., JPA entities, static utility methods).
 * <p>
 * NOTE: Use sparingly. Prefer constructor injection where possible.
 */
@Component
@RequiredArgsConstructor
public class AppConfigHolder {

    private final AppProperties appProperties;

    private static AppProperties staticAppProperties;

    @PostConstruct
    public void init() {
        staticAppProperties = this.appProperties;
    }

    /**
     * Get OTP expiration time in minutes.
     * Used by VerificationCode entity for setting expiration timestamp.
     */
    public static int getOtpExpirationMinutes() {
        if (staticAppProperties == null) {
            return 10;
        }
        return staticAppProperties.getAuth().getOtp().getExpirationMinutes();
    }

    /**
     * Get AWS bucket name.
     */
    public static String getAwsBucketName() {
        if (staticAppProperties == null) {
            throw new IllegalStateException("AppConfigHolder not initialized");
        }
        return staticAppProperties.getAws().getBucketName();
    }

    /**
     * Get presigned URL expiration for GET requests (in minutes).
     */
    public static int getPresignGetExpiryMinutes() {
        if (staticAppProperties == null) {
            return 10;
        }
        return staticAppProperties.getAws().getPresign().getGetExpiryMinutes();
    }

    /**
     * Get presigned URL expiration for PUT requests (in minutes).
     */
    public static int getPresignPutExpiryMinutes() {
        if (staticAppProperties == null) {
            return 15;
        }
        return staticAppProperties.getAws().getPresign().getPutExpiryMinutes();
    }
}

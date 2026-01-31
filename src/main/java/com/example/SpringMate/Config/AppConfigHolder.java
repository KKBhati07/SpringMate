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
     * Provides static access for JPA entity callbacks where DI is unavailable.
     */
    public static int getOtpExpirationMinutes() {
        if (staticAppProperties == null) {
            return 10;
        }
        return staticAppProperties.getAuth().getOtp().getExpirationMinutes();
    }

    public static String getAwsBucketName() {
        if (staticAppProperties == null) {
            throw new IllegalStateException("AppConfigHolder not initialized");
        }
        return staticAppProperties.getAws().getBucketName();
    }

    public static int getPresignGetExpiryMinutes() {
        if (staticAppProperties == null) {
            return 10;
        }
        return staticAppProperties.getAws().getPresign().getGetExpiryMinutes();
    }

    public static int getPresignPutExpiryMinutes() {
        if (staticAppProperties == null) {
            return 15;
        }
        return staticAppProperties.getAws().getPresign().getPutExpiryMinutes();
    }
}

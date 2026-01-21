package com.example.SpringMate.Util;

import com.example.SpringMate.Filter.RequestIdFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Standardized API response wrapper for consistent response structure across all endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private T data;
    private String message;
    private Instant timestamp;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Creates a successful response with data and message.
     */
    public static <T> Response<T> success(T data, String message) {
        return Response.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response with only data (message will be null).
     */
    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response with message.
     */
    public static <T> Response<T> error(String message) {
        return Response.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response with message and metadata.
     */
    public static <T> Response<T> error(String message, Map<String, Object> metadata) {
        return Response.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .metadata(metadata != null ? metadata : new HashMap<>())
                .build();
    }

    /**
     * Adds metadata to the response.
     */
    public Response<T> withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Adds multiple metadata entries to the response.
     */
    public Response<T> withMetadata(Map<String, Object> additionalMetadata) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        if (additionalMetadata != null) {
            this.metadata.putAll(additionalMetadata);
        }
        return this;
    }
}

package com.example.SpringMate.Storage.DTO;

import java.time.Instant;

public record PresignResult(String url, String objectKey, Instant expiresAt, String contentType) {
}


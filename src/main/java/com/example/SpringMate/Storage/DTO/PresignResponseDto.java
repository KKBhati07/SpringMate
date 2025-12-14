package com.example.SpringMate.Storage.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class PresignResponseDto {
    private String url, objectKey;
    Map<String, String> headers;
    Boolean isCover;
    Instant expiresAt;

}
package com.example.SpringMate.Auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Builder
@Data
public class OtpLoginResponseDto {
    private UUID userUuid;
    private boolean authenticated;
}

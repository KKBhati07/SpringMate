package com.example.SpringMate.Util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class UserDetailsDto {
    private String name;
    private String email;
    private UUID uuid;
    private boolean isAdmin;
    private String contactNo;
    private String profileUrl;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean deleted;
}
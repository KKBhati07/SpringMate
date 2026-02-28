package com.example.SpringMate.Listing.DTO;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

    private Long id;
    private String name;
    private String profileUrl;
    private UUID uuid;
    private boolean deleted;
    private boolean emailVerified;

}

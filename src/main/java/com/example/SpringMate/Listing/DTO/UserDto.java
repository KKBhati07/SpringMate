package com.example.SpringMate.Listing.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

    private Long id;
    private String name;
    private String profileUrl;
    private boolean deleted;
    private boolean emailVerified;

}

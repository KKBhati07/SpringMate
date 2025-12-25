package com.example.SpringMate.Auth.Cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CachedAuthentication implements Serializable {

    private Long userId;
    private UUID userUuid;
    private String email;
    private String name;
    private boolean admin;
    private List<String> authorities;
}

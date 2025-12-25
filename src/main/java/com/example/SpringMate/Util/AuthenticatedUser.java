package com.example.SpringMate.Util;

import java.util.UUID;

public record AuthenticatedUser(
        Long id,
        UUID uuid,
        String email,
        String name,
        boolean isAdmin
) {}

package com.example.SpringMate.Shared;

/**
 * Roles for authorization
 */
public final class Roles {

    private Roles() {}

    /**
     * DB roles
     */
    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String PROMETHEUS = "PROMETHEUS";

    /**
     * Spring authorities
     */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_PROMETHEUS = "ROLE_PROMETHEUS";
}

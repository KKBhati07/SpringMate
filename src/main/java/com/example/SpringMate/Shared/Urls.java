package com.example.SpringMate.Shared;

public class Urls {
    private Urls(){}
    public static final String BASE_URL="/api/v1";

    public static final class Auth {
        public static final String AUTH_BASE_URL = BASE_URL + "/auth";
        public static final String LOGIN_URL = "/login";
        public static final String LOGOUT_URL = "/logout";
        public static final String AUTH_DETAILS = "/auth_details";
        public static final String REQUEST_LOGIN_OTP = "/request_login_otp";
        public static final String OTP_LOGIN = "/login_with_otp";
    }

    public static final class Admin {
        public static final String ADMIN_BASE = BASE_URL + "/admin";

        public static final class User {
            public static final String FETCH_ALL = "/users/get_all";
            public static final String DELETE = "/users/delete_user/{uuid}";
            public static final String RESTORE = "/users/restore_user/{uuid}";
            public static final String UPDATE = "/users/update_user";
        }

        public static final class Listing {

        }
    }

    public static final class User {
        public static final String USER_BASE = BASE_URL + "/user";
        public static final String CREATE_USER = "/create_user";
        public static final String UPDATE_USER = "/update_user";
        public static final String DELETE_USER = "/delete_user";
        public static final String GET_DETAILS = "/{uuid}/get_details";

    }

    public static final class Category {
        public static final String CATEGORY_BASE = BASE_URL + "/category";
        public static final String GET_ALL = "/get_all";
    }

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/user/create_user",
            "/api/v1/category/**",
            "/api/v1/auth/request_login_otp",
            "/api/v1/auth/login_with_otp"
    };
}

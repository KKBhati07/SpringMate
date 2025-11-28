package com.example.SpringMate.Shared;

public class Urls {
    private Urls(){}

    public static final String BASE_URL = "/api/v1";

    public static final class Auth {
        public static final String AUTH_BASE = BASE_URL + "/auth";
        public static final String LOGIN_WITH_PASS = "/login_with_password";
        public static final String LOGOUT = "/logout";
        public static final String AUTH_DETAILS = "/auth_details";
        public static final String REQUEST_LOGIN_OTP = "/request_login_otp";
        public static final String OTP_LOGIN = "/login_with_otp";
    }

    public static final class Admin {
        public static final String ADMIN_BASE = BASE_URL + "/admin";

        public static final class User {
            public static final String GET_ALL = "/users/get_all";
            public static final String DELETE = "/users/delete_user/{uuid}";
            public static final String RESTORE = "/users/restore_user/{uuid}";
            public static final String UPDATE = "/users/update_user";
        }

        public static final class Listing {
            public static final String GET_ALL = "/listings/get_all";
            public static final String DELETE = "/listings/delete";

        }
    }

    public static final class User {
        public static final String USER_BASE = BASE_URL + "/user";
        public static final String CREATE_USER = "/create_user";
        public static final String UPDATE_USER = "/update_user";
        public static final String DELETE_USER = "/delete_user";
        public static final String GET_DETAILS = "/{uuid}/get_details";

    }

    public static final class Listing {
        public static final String LISTING_BASE = BASE_URL + "/listing";
        public static final String GET_ALL = "/get_all";
        public static final String GET_BY_USER = "/get_by_user";
        public static final String GET_FAVORITES = "/get_favorites";
        public static final String CREATE_LISTING = "/create";
        public static final String UPDATE_LISTING = "/update";
        public static final String DELETE_LISTING = "/delete";
        public static final String GET_DETAILS = "/{uuid}/get_details";

    }


    public static final class Category {
        public static final String CATEGORY_BASE = BASE_URL + "/category";
        public static final String GET_ALL = "/get_all";
    }

    public static final class Location {
        public static final String LOCATION_BASE = BASE_URL + "/location";
        //        public static final String GET_ALL = "/get_all";
        public static final String SEED = "/seed";
        public static final String GET_COUNTRIES = "/get_countries";
        public static final String GET_STATES = "/get_states";
        public static final String GET_CITIES = "/get_cities";
    }

    public static final class UserFavorites {
        public static final String FAVORITE_BASE = BASE_URL + "/favorite";
        public static final String SET_UNSET = "/set_unset";

    }

    public static final class ExternalApi {
        public static final class Locations {
            public static final String BASE_URL = "https://api.countrystatecity.in/v1";
            public static final String COUNTRIES = BASE_URL + "/countries";
            public static final String STATES_BY_COUNTRY = BASE_URL + "/countries/{iso2}/states";
            public static final String CITIES_BY_STATE = BASE_URL + "/countries/{country_iso2}/states/{state_iso2}/cities";
        }

    }

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/user/create_user",
            "/api/v1/category/**",
            "/api/v1/auth/request_login_otp",
            "/api/v1/auth/login_with_otp",
            "/api/v1/listing/get_all",
            "/api/v1/listing/get_by_user",
            "/api/v1/location/**"

    };
}

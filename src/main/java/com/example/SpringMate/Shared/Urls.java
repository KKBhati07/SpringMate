package com.example.SpringMate.Shared;

/**
 * Centralized URL constants for all API endpoints.
 */
public final class Urls {

    private Urls() {
    }

    public static final class Api {
        public static final String BASE_PATH = "/api";
        public static final String VERSION = "v1";
        public static final String ROOT = BASE_PATH + "/" + VERSION;
    }

    public static final class Auth {
        public static final String BASE = Api.ROOT + "/auth";

        public static final String LOGIN_WITH_PASS = "/login_with_password";
        public static final String LOGOUT = "/logout";
        public static final String AUTH_DETAILS = "/auth_details";
        public static final String REQUEST_LOGIN_OTP = "/request_login_otp";
        public static final String OTP_LOGIN = "/login_with_otp";
        public static final String RESOLVE_SESSION = "/resolve_session";
    }

    public static final class Admin {
        public static final String BASE = Api.ROOT + "/admin";

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
        public static final String BASE = Api.ROOT + "/user";

        public static final String CREATE_USER = "/create_user";
        public static final String UPDATE_USER = "/update_user";
        public static final String UPLOAD_IMAGE_FALLBACK = "/upload_image_fallback";
        public static final String DELETE_USER = "/delete_user";
        public static final String GET_DETAILS = "/{uuid}/get_details";
    }

    public static final class Listing {
        public static final String BASE = Api.ROOT + "/listing";

        public static final String GET_ALL = "/get_all";
        public static final String GET_BY_USER = "/get_by_user";
        public static final String GET_FAVORITES = "/get_favorites";
        public static final String GET_CONDITIONS = "/get_conditions";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update";
        public static final String DELETE = "/delete";
        public static final String IMAGE_UPLOAD_FALLBACK = "/image_upload_fallback";
        public static final String GET_DETAILS = "/{id}/get_details";
        public static final String CONTACT_SELLER_EMAIL = "/{id}/contact_seller_email";
    }

    public static final class Category {
        public static final String BASE = Api.ROOT + "/category";
        public static final String GET_ALL = "/get_all";
    }

    public static final class Storage {
        public static final String BASE = Api.ROOT + "/storage";

        public static final String PRESIGN_URL = "/presign_url";
        public static final String OBJECT_EXISTS = "/object_exists";
    }

    public static final class Location {
        public static final String BASE = Api.ROOT + "/location";

        public static final String SEED = "/seed";
        public static final String GET_COUNTRIES = "/get_countries";
        public static final String GET_STATES = "/get_states";
        public static final String GET_CITIES = "/get_cities";
    }

    public static final class UserFavorites {
        public static final String BASE = Api.ROOT + "/favorite";
        public static final String SET = "/{listing_id}";
    }

    public static final class ExternalApi {
        public static final class Locations {
            public static final String BASE_URL = "https://api.countrystatecity.in/v1";
            public static final String COUNTRIES = BASE_URL + "/countries";
            public static final String STATES_BY_COUNTRY = BASE_URL + "/countries/{iso2}/states";
            public static final String CITIES_BY_STATE = BASE_URL + "/countries/{country_iso2}/states/{state_iso2}/cities";
        }
    }

    public static final class Actuator {
        public static final String BASE = "/actuator";

        public static final String HEALTH = BASE + "/health";
        public static final String INFO = BASE + "/info";
        public static final String METRICS = BASE + "/metrics";
        public static final String PROMETHEUS = BASE + "/prometheus";
        public static final String ENV = BASE + "/env";
        public static final String BEANS = BASE + "/beans";
        public static final String MAPPINGS = BASE + "/mappings";
        public static final String LOGGERS = BASE + "/loggers";
        public static final String THREADDUMP = BASE + "/threaddump";
        public static final String HEAPDUMP = BASE + "/heapdump";
    }

    public static final class Security {

        public static final String[] PUBLIC_ENDPOINTS = {
                User.BASE + User.CREATE_USER,
                Category.BASE + "/**",
                Auth.BASE + Auth.REQUEST_LOGIN_OTP,
                Auth.BASE + Auth.OTP_LOGIN,
                Listing.BASE + Listing.GET_ALL,
                Listing.BASE + Listing.GET_BY_USER,
                Listing.BASE + Listing.GET_DETAILS,
                Location.BASE + "/**",
                Internal.Auth.BASE + "/**"
        };

        public static final String[] FILTER_EXCLUDED_ENDPOINTS = {
                User.BASE + User.CREATE_USER,
                Auth.BASE + Auth.LOGIN_WITH_PASS,
                Auth.BASE + Auth.REQUEST_LOGIN_OTP,
                Auth.BASE + Auth.OTP_LOGIN,
                Location.BASE + "/**",
                Actuator.PROMETHEUS,
                Internal.Auth.BASE + "/**"
        };
    }

    public static class Internal {
        public static final String INTERNAL_ROOT = "/internal/v1";

        public static class Auth {
            public static final String BASE = INTERNAL_ROOT + "/auth";
            public static final String RESOLVE_SESSION = "/resolve_session";
        }
    }

}

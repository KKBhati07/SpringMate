package com.example.SpringMate.Shared;

public class Constants {
    private Constants() {
    }

    //TODO :: Move it to ENV
    public static class AWS {
        public static String REGION = "ap-south-1";
        public static String BUCKET_NAME = "marketmatestore";
        public static int GET_SIGNED_URI_EXPIRATION = 10;
        public static int PUT_SIGNED_URI_EXPIRATION = 15;
    }

    public static class Origin {
        public static class Frontend {
            //            public static final String DEV = "http://localhost:4200";
            public static final String DEV = "https://marketmate.local:4200";
        }

        public static class AdminPortal {
            //            public static final String DEV = "http://localhost:4300";
            public static final String DEV = "https://admin.marketmate.local:4300";
        }

        public static final String DEV = "http://localhost:4200";
        public static final String PROD = "";
    }

    public static final class UserRole {
        public static final String USER = "USER";
        public static final String ADMIN = "ADMIN";
    }

    public static final class CacheNamespace {
        public static final String CATEGORY = "categories";
        public static final String COUNTRY = "countries";
        public static final String STATE = "states";
        public static final String CITY = "cities";
    }

    public static final String[] CATEGORIES = {"cars", "bikes", "mobile_phones", "electronic", "furniture", "property", "others"};

    public static final int SESSION_VALIDITY = 7;
    public static final int JWT_VALIDITY = 7;
    public static final int OTP_EXPIRATION_MINUTES = 10;

    public static class EmailHeaders {
        public static final String LOGIN = "OTP Verification";
    }

    public static class Images {
        public static class Listing {
            public static final int MAX_LIMIT = 6;

        }
    }

    public static class AppContext {
        public static final String ADMIN = "ADMIN";
        public static final String PUBLIC = "PUBLIC";

    }

    public static final String DEFAULT_CATEGORY = "others";

    public static final String COOKIE_DOMAIN = ".marketmate.local";

    public static final class Messages {
        public static final class Error {

            public static final String SOMETHING_WENT_WRONG = "Something went wrong!";
            public static final String BAD_REQUEST = "Bad Request!";
            public static final String FORBIDDEN = "Forbidden";
            public static final String UNAUTHORIZED = "Unauthorized";
            public static final String NOT_FOUND = "Requested resource not found";

        }
    }
}

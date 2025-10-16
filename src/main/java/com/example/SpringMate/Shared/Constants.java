package com.example.SpringMate.Shared;

public class Constants {
    private Constants() {
    }

    //TODO :: Move it to ENV
    public static class AWS {
        public static String REGION = "ap-south-1";
        public static String BUCKET_NAME = "marketmatestore";
        public static int SIGNED_URI_EXPIRATION = 10;
    }

    public static class Origin {
        public static final String DEV = "http://localhost:4200";
        public static final String PROD = "";
    }

    public static final class UserRole {
        public static final String USER = "USER";
        public static final String ADMIN = "ADMIN";
    }

    public static final String[] CATEGORIES = {"cars", "bikes", "mobile_phones", "electronic", "furniture", "property", "others"};

    public static final int SESSION_VALIDITY = 30;
    public static final int OTP_EXPIRATION_MINUTES = 10;

    public static class EmailHeaders {
        public static final String LOGIN = "OTP Verification";
    }

    public static final String DEFAULT_CATEGORY = "others";

    public static final class Messages {
        public static final class Error {

            public static final String SOMETHING_WENT_WRONG = "Something went wrong!";
        }
    }
}

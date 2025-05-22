package com.example.SpringMate.Util;

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
}

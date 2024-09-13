package com.example.SpringMate.Util;

public class Constants {
    private Constants() {}

    public static class Origin{
        public static final String DEV = "http://localhost:4200";
        public static final String PROD = "";
    }

    public static final class UserRole{
        public static final String USER = "USER";
        public static final String ADMIN = "ADMIN";
    }

    public static final String [] CATEGORIES = {"cars", "bikes", "mobile_phones", "electronics", "furniture", "property", "others"};
}

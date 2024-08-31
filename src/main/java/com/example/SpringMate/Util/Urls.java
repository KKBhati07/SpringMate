package com.example.SpringMate.Util;

public class Urls {
    private Urls(){}
    public static final String BASE_URL="/api/v1";

    public static final class Auth{
        public static final String AUTH_BASE_URL=BASE_URL+"/auth";
        public static final String LOGIN_URL ="/login";
        public static final String LOGOUT_URL ="/logout";
        public static final String AUTH_DETAILS ="/auth_details";
    }

    public static final class Admin{
         public static final String ADMIN_BASE= BASE_URL+ "/admin";
         public static final class User{
             public static final String FETCH_ALL="/users/fetch_all";
         }

         public static final class Listing{

         }
    }
    public static final class User{
        public static final String USER_BASE = BASE_URL+ "/user";
        public static final String CREATE_USER="/create_user";
        public static final String UPDATE_USER="/update_user";
        public static final String DELETE_USER="/delete_user";

    }
}

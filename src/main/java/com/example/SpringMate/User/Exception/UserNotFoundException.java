package com.example.SpringMate.User.Exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(){
        super("User not found");
    }

    public UserNotFoundException(String msg){
        super(msg);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.example.SpringMate.User.Exception;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(){
        super("User Already Exists");
    }

    public UserAlreadyExistsException(String msg){
        super(msg);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.example.SpringMate.User.Exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException() {
        super("Email already in use");
    }

    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}

package com.example.SpringMate.User.Exception;

public class UnauthorizedUserUpdateException extends RuntimeException {
    public UnauthorizedUserUpdateException() {
        super("Cannot update another user's profile");
    }

    public UnauthorizedUserUpdateException(String message) {
        super(message);
    }
}
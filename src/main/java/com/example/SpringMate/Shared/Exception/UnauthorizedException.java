package com.example.SpringMate.Shared.Exception;

import com.example.SpringMate.Shared.Constants;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super(Constants.Messages.Error.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

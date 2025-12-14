package com.example.SpringMate.Shared.Exception;

import com.example.SpringMate.Shared.Constants;

public class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super(Constants.Messages.Error.BAD_REQUEST);
    }
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

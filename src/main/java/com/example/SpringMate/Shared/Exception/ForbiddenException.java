package com.example.SpringMate.Shared.Exception;

import com.example.SpringMate.Shared.Constants;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super(Constants.Messages.Error.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

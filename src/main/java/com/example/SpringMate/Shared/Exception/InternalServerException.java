package com.example.SpringMate.Shared.Exception;

import com.example.SpringMate.Shared.Constants;

public class InternalServerException extends RuntimeException {
    public InternalServerException() {
        super(Constants.Messages.Error.SOMETHING_WENT_WRONG);
    }

    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}

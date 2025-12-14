package com.example.SpringMate.Shared.Exception;

import com.example.SpringMate.Shared.Constants;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super(Constants.Messages.Error.NOT_FOUND);
    }

    public NotFoundException(String msg) {
        super(msg);
    }

}

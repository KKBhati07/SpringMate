package com.example.SpringMate.Exceptions;

import org.springframework.security.core.AuthenticationException;

public class MissingCredentialsException extends AuthenticationException {
    public MissingCredentialsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public MissingCredentialsException(String msg) {
        super(msg);
    }


}

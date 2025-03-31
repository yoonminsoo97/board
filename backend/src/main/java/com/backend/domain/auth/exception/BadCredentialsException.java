package com.backend.domain.auth.exception;

import com.backend.global.error.exception.ErrorType;

import org.springframework.security.core.AuthenticationException;

public class BadCredentialsException extends AuthenticationException {

    public BadCredentialsException() {
        super(ErrorType.BAD_CREDENTIALS.getErrorCode());
    }

}

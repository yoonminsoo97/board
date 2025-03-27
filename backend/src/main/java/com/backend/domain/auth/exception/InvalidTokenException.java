package com.backend.domain.auth.exception;

import com.backend.global.error.exception.ErrorType;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException() {
        super(ErrorType.INVALID_TOKEN.getErrorCode());
    }

}

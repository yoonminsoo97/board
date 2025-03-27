package com.backend.domain.auth.exception;

import com.backend.global.error.exception.ErrorType;

import org.springframework.security.core.AuthenticationException;

public class ExpiredTokenException extends AuthenticationException {

    public ExpiredTokenException() {
        super(ErrorType.EXPIRED_TOKEN.getErrorCode());
    }

}

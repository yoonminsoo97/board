package com.board.domain.token.exception;

import com.board.global.error.ErrorType;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException() {
        super(ErrorType.INVALID_TOKEN.getErrorCode());
    }

}

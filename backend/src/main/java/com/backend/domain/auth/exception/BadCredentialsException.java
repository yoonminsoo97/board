package com.backend.domain.auth.exception;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class BadCredentialsException extends BoardException {

    public BadCredentialsException() {
        super(ErrorType.BAD_CREDENTIALS);
    }

}

package com.backend.domain.auth.exception;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class NotFoundTokenException extends BoardException {

    public NotFoundTokenException() {
        super(ErrorType.NOT_FOUND_TOKEN);
    }

}

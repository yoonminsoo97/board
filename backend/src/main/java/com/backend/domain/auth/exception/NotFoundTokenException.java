package com.backend.domain.auth.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.NotFoundException;

public class NotFoundTokenException extends NotFoundException {

    public NotFoundTokenException() {
        super(ErrorType.NOT_FOUND_TOKEN);
    }

}

package com.backend.global.error.exception.type;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class NotFoundException extends BoardException {

    public NotFoundException(ErrorType errorType) {
        super(errorType);
    }

}

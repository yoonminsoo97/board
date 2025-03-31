package com.backend.global.error.exception.type;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class AccessDeniedException extends BoardException {

    public AccessDeniedException(ErrorType errorType) {
        super(errorType);
    }

}

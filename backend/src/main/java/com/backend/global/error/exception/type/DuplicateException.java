package com.backend.global.error.exception.type;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class DuplicateException extends BoardException {

    public DuplicateException(ErrorType errorType) {
        super(errorType);
    }

}

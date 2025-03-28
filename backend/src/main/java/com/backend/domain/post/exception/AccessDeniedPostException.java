package com.backend.domain.post.exception;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class AccessDeniedPostException extends BoardException {

    public AccessDeniedPostException() {
        super(ErrorType.ACCESS_DENIED_POST);
    }

}

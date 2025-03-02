package com.backend.domain.post.exception;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class NotFoundPostException extends BoardException {

    public NotFoundPostException() {
        super(ErrorType.NOT_FOUND_POST);
    }

}

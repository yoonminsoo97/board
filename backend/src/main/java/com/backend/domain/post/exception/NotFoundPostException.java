package com.backend.domain.post.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.NotFoundException;

public class NotFoundPostException extends NotFoundException {

    public NotFoundPostException() {
        super(ErrorType.NOT_FOUND_POST);
    }

}

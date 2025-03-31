package com.backend.domain.comment.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.NotFoundException;

public class NotFoundCommentException extends NotFoundException {

    public NotFoundCommentException() {
        super(ErrorType.NOT_FOUND_COMMENT);
    }

}

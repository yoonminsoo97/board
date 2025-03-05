package com.backend.domain.comment.exception;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class NotFoundCommentException extends BoardException {

    public NotFoundCommentException() {
        super(ErrorType.NOT_FOUND_COMMENT);
    }

}

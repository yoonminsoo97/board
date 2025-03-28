package com.backend.domain.comment.exception;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class AccessDeniedCommentException extends BoardException {

    public AccessDeniedCommentException() {
        super(ErrorType.ACCESS_DENIED_COMMENT);
    }

}

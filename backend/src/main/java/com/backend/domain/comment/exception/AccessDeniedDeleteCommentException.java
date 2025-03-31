package com.backend.domain.comment.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.AccessDeniedException;

public class AccessDeniedDeleteCommentException extends AccessDeniedException {

    public AccessDeniedDeleteCommentException() {
        super(ErrorType.ACCESS_DENIED_DELETE_COMMENT);
    }

}

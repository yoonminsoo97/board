package com.backend.domain.comment.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.AccessDeniedException;

public class AccessDeniedModifyCommentException extends AccessDeniedException {

    public AccessDeniedModifyCommentException() {
        super(ErrorType.ACCESS_DENIED_MODIFY_COMMENT);
    }

}

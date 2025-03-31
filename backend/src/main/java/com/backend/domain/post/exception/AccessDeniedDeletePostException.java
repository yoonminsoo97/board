package com.backend.domain.post.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.AccessDeniedException;

public class AccessDeniedDeletePostException extends AccessDeniedException {

    public AccessDeniedDeletePostException() {
        super(ErrorType.ACCESS_DENIED_DELETE_POST);
    }

}

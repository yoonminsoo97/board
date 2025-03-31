package com.backend.domain.post.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.AccessDeniedException;

public class AccessDeniedModifyPostException extends AccessDeniedException {

    public AccessDeniedModifyPostException() {
        super(ErrorType.ACCESS_DENIED_MODIFY_POST);
    }

}

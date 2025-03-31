package com.backend.domain.member.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.DuplicateException;

public class DuplicateUsernameException extends DuplicateException {

    public DuplicateUsernameException() {
        super(ErrorType.DUPLICATE_USERNAME);
    }

}

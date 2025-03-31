package com.backend.domain.member.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.DuplicateException;

public class DuplicateNicknameException extends DuplicateException {

    public DuplicateNicknameException() {
        super(ErrorType.DUPLICATE_NICKNAME);
    }

}

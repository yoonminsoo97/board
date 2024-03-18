package com.board.domain.member.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class PasswordMismatchException extends BaseException {

    public PasswordMismatchException() {
        super(ErrorType.PASSWORD_MISMATCH);
    }

}

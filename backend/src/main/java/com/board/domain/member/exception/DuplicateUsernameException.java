package com.board.domain.member.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class DuplicateUsernameException extends BaseException {

    public DuplicateUsernameException() {
        super(ErrorType.DUPLICATE_USERNAME);
    }

}

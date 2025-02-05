package com.board.domain.member.exception;

import com.board.global.error.exception.APIException;
import com.board.global.error.exception.ErrorType;

public class DuplicateUsernameException extends APIException {

    public DuplicateUsernameException() {
        super(ErrorType.DUPLICATE_USERNAME);
    }

}

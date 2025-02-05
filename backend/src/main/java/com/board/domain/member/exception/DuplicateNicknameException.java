package com.board.domain.member.exception;

import com.board.global.error.exception.APIException;
import com.board.global.error.exception.ErrorType;

public class DuplicateNicknameException extends APIException {

    public DuplicateNicknameException() {
        super(ErrorType.DUPLICATE_NICKNAME);
    }

}

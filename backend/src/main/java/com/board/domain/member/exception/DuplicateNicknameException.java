package com.board.domain.member.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class DuplicateNicknameException extends BaseException {

    public DuplicateNicknameException() {
        super(ErrorType.DUPLICATE_NICKNAME);
    }

}

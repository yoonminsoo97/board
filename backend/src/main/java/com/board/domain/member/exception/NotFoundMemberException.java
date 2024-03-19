package com.board.domain.member.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class NotFoundMemberException extends BaseException {

    public NotFoundMemberException() {
        super(ErrorType.NOT_FOUND_MEMBER);
    }

}

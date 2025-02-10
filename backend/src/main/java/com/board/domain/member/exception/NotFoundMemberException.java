package com.board.domain.member.exception;

import com.board.global.error.exception.APIException;
import com.board.global.error.exception.ErrorType;

public class NotFoundMemberException extends APIException {

    public NotFoundMemberException() {
        super(ErrorType.NOT_FOUND_MEMBER);
    }

}

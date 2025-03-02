package com.backend.domain.member.exception;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

public class NotFoundMemberException extends BoardException {

    public NotFoundMemberException() {
        super(ErrorType.NOT_FOUND_MEMBER);
    }

}

package com.backend.domain.member.exception;

import com.backend.global.error.exception.ErrorType;
import com.backend.global.error.exception.type.NotFoundException;

public class NotFoundMemberException extends NotFoundException {

    public NotFoundMemberException() {
        super(ErrorType.NOT_FOUND_MEMBER);
    }

}

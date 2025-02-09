package com.board.domain.token.exception;

import com.board.global.error.exception.APIException;
import com.board.global.error.exception.ErrorType;

public class NotFoundTokenException extends APIException {

    public NotFoundTokenException() {
        super(ErrorType.NOT_FOUND_TOKEN);
    }

}

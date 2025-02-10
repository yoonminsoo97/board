package com.board.domain.post.exception;

import com.board.global.error.exception.APIException;
import com.board.global.error.exception.ErrorType;

public class NotFoundPostException extends APIException {

    public NotFoundPostException() {
        super(ErrorType.NOT_FOUND_POST);
    }

}

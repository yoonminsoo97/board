package com.board.domain.post.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class NotFoundPostException extends BaseException {

    public NotFoundPostException() {
        super(ErrorType.NOT_FOUND_POST);
    }

}

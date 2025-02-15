package com.board.domain.comment.exception;

import com.board.global.error.exception.APIException;
import com.board.global.error.exception.ErrorType;

public class NotFoundCommentException extends APIException {

    public NotFoundCommentException() {
        super(ErrorType.NOT_FOUND_COMMENT);
    }

}

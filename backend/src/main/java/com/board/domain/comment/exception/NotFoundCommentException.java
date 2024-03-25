package com.board.domain.comment.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class NotFoundCommentException extends BaseException {

    public NotFoundCommentException() {
        super(ErrorType.NOT_FOUND_COMMENT);
    }

}

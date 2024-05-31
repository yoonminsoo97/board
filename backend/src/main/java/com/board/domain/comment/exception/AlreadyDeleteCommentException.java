package com.board.domain.comment.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class AlreadyDeleteCommentException extends BaseException {

    public AlreadyDeleteCommentException() {
        super(ErrorType.ALREADY_COMMENT_DELETE);
    }

}

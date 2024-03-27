package com.board.domain.comment.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class CommentDeleteAccessDeniedException extends BaseException {

    public CommentDeleteAccessDeniedException() {
        super(ErrorType.COMMENT_DELETE_ACCESS_DENIED);
    }

}

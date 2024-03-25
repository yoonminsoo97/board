package com.board.domain.comment.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class CommentModifyAccessDeniedException extends BaseException {

    public CommentModifyAccessDeniedException() {
        super(ErrorType.COMMENT_MODIFY_ACCESS_DENIED);
    }

}

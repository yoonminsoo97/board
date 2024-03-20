package com.board.domain.post.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class PostModifyAccessDeniedException extends BaseException {

    public PostModifyAccessDeniedException() {
        super(ErrorType.POST_MODIFY_ACCESS_DENIED);
    }

}

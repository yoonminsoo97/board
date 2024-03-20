package com.board.domain.post.exception;

import com.board.global.error.ErrorType;
import com.board.global.error.exception.BaseException;

public class PostDeleteAccessDeniedException extends BaseException {

    public PostDeleteAccessDeniedException() {
        super(ErrorType.POST_DELETE_ACCESS_DENIED);
    }

}

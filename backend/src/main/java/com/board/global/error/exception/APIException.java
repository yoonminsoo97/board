package com.board.global.error.exception;

import lombok.Getter;

@Getter
public class APIException extends RuntimeException {

    private final ErrorType errorType;

    public APIException(ErrorType errorType) {
        this.errorType = errorType;
    }

}

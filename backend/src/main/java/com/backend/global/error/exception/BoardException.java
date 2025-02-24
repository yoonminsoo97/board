package com.backend.global.error.exception;

import lombok.Getter;

@Getter
public class BoardException extends RuntimeException {

    private final ErrorType errorType;

    public BoardException(ErrorType errorType) {
        this.errorType = errorType;
    }

}

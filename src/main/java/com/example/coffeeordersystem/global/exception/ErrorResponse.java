package com.example.coffeeordersystem.global.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final String errorCode;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.errorCode = errorCode.name();
        this.message = errorCode.getMessage();
    }
}

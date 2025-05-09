package com.example.enterpriseapp.exception;

public class CustomException extends RuntimeException {

    private final int errorCode;

    public CustomException(int errorCode, String message) {
        super(message); // nastaví message v RuntimeException
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}

package com.crewmeister.cmcodingchallenge.exception;

public class ExchangeRateException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public ExchangeRateException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
} 
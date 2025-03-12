package com.jubeiwato.costing_service.authentication.config;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
     private final HttpStatus status;
     private final String message;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
    }
     
    @Override
    public String getMessage() {
        return message;
    }
    public HttpStatus getStatus() {
        return status;
    }
}

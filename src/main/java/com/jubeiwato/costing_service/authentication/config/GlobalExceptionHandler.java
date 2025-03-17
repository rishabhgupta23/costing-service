package com.jubeiwato.costing_service.authentication.config;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.ErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {AppException.class, InternalAuthenticationServiceException.class})
    public ResponseEntity<ErrorResponseDto> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ErrorResponseDto.of(ex.getMessage(), ex.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.of(ErrorMessageConstant.UNEXPECTED_ERROR_OCCURED, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
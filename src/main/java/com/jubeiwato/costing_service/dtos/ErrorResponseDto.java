package com.jubeiwato.costing_service.dtos;
import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ErrorResponseDto {
        private String message;
    private HttpStatus status;
  
        public static ErrorResponseDto of(String message, HttpStatus status) {
        return new ErrorResponseDto(message, status);
    }
}

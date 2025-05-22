package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GeneralResponseDto {
    private String message;
    private int status;
}

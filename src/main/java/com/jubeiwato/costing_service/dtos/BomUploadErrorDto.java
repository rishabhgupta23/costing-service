package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BomUploadErrorDto {

    private int rowNumber;
    private String message;
}

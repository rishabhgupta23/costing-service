package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExcelBomRow {

    private int rowNumber;
    private String partNumber;
    private String partName;
    private String type;
    private String unit;
    private Double quantity;
}

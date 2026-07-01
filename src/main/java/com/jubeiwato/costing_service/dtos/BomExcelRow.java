package com.jubeiwato.costing_service.dtos;

import lombok.Data;

@Data
public class BomExcelRow {

    private String type;

    private String partNumber;

    private String partName;

    private String unit;

    private Double quantity;

}

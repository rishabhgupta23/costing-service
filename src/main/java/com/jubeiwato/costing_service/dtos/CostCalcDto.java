package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostCalcDto {
    private String partName;
    private String partNumber;
    private Double quantity;
    private Double price;
    private String vendorName;
    private Double rate;
}

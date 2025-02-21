package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostFactorValueDto extends CostFactorDto{
    Double value;

    public CostFactorValueDto(Long id, String name, Double value) {
        super(id, name);
        this.value = value;
    }
}

package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.CostFactor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  CostFactorDto {
    private Long id;
    private String factorName;
    private Double value;

    public static CostFactorDto entityToDto(CostFactor costFactor,Double value) {
        return CostFactorDto.builder()
        .id(costFactor.getFactorId())
        .factorName(costFactor.getFactorName())
                .value(value)
        .build();
    }
}

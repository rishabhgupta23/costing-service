package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.CostFactor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CostFactorDto {
    private Long id;
    private String name;

    public static CostFactorDto entityToDto(CostFactor costFactor) {
        return CostFactorDto.builder()
        .id(costFactor.getFactorId())
        .name(costFactor.getFactorName())
        .build();
    }
}

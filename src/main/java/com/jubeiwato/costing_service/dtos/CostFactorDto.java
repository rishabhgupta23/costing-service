package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.CostFactor;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "CostFactor name cannot be empty or null")
    private String name;
    private Double value;

    public static CostFactorDto entityToDto(CostFactor costFactor,Double value) {
        return CostFactorDto.builder()
        .id(costFactor.getFactorId())
        .name(costFactor.getFactorName())
                .value(value)
        .build();
    }
}

package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.constants.CostFactorType;
import com.jubeiwato.costing_service.entities.CostFactor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostFactorDto {

    private Long id;

    @NotBlank(message = "Factor name is required")
    private String factorName;

    private Double quantity;

    private Double rate;

    private Double value;

    @NotNull(message = "Factor type is required")
    private CostFactorType factorType;

    private String comments;

    public static CostFactorDto entityToDto(
            CostFactor costFactor,
            Double quantity,
            Double rate,
            Double value,
            String comments) {

        return CostFactorDto.builder()
                .id(costFactor.getFactorId())
                .factorName(costFactor.getFactorName())
                .quantity(quantity)
                .rate(rate)
                .value(value)
                .factorType(costFactor.getFactorType())
                .comments(comments)
                .build();
    }
}
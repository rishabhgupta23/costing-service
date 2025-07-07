package com.jubeiwato.costing_service.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPlanRequestDto {
    @NotEmpty(message = "Parts list cannot be empty")
    private List<ProductionPlanPartDto> parts;

    @NotNull(message = "Pricing mode must be provided")
    private String priceMode;
}

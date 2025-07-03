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
public class ProductionPriceRequestDto {
    @NotEmpty(message = "Parts list cannot be empty")
    private List<ProductionPlanRequestDto> parts;

    @NotNull(message = "Pricing mode must be provided")
    private String priceMode;
}

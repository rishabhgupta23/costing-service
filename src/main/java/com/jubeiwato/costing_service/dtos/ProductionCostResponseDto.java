package com.jubeiwato.costing_service.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionCostResponseDto {
    private List<CostItemDto> items;
    private Double totalCost;
}

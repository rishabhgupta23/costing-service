package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.dtos.ProductionCostResponseDto;
import com.jubeiwato.costing_service.dtos.ProductionRequestDto;

public interface ProductionCostService {
    public ProductionCostResponseDto calculateProductionCost(List<ProductionRequestDto> parts, String priceMode, Long companyId);
}

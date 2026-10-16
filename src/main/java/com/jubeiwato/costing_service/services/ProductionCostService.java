package com.jubeiwato.costing_service.services;

import java.io.IOException;
import java.util.List;

import com.jubeiwato.costing_service.dtos.ProductionCostResponseDto;
import com.jubeiwato.costing_service.dtos.ProductionPlanPartDto;

public interface ProductionCostService {
    public ProductionCostResponseDto calculateProductionCost(List<ProductionPlanPartDto> parts, String priceMode, Long companyId);
    byte[] downloadProductionPlanExcel(
            List<ProductionPlanPartDto> parts,
            String priceMode,
            Long companyId
    ) throws IOException;

}

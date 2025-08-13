package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.CostCalcResultDto;
import com.jubeiwato.costing_service.dtos.CostItemDto;



public interface CostCalcService {
    public CostCalcResultDto calculatePrice(Long partId, String priceMode, Long companyId);
    public CostItemDto calculateUnitPart(Long partId, String priceMode, Double quantity);

}

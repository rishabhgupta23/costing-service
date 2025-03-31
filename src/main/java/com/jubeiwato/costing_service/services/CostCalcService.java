package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.CostCalcResultDto;



public interface CostCalcService {
    public CostCalcResultDto calculatePrice(Long partId, String priceMode, Long companyId);
}

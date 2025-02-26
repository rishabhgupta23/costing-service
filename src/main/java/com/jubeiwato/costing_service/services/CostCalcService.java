package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.ResultCostDto;



public interface CostCalcService {
    public ResultCostDto calculatePrice(Long partId, String priceMode);
}

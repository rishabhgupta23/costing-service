package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.CostCalcDto;


public interface CostCalcService {
    public CostCalcDto calculatePrice(Long partId, String priceMode);
}

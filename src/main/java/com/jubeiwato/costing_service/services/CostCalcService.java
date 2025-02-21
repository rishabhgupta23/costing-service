package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.CostCalcDto;

import java.util.List;


public interface CostCalcService {
    public List<CostCalcDto> calculatePrice(Long partId, String priceMode);
}

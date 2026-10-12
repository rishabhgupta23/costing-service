package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.CostCalcResultDto;
import com.jubeiwato.costing_service.dtos.CostItemDto;

import java.io.IOException;



public interface CostCalcService {

    CostCalcResultDto calculatePrice(Long partId, String priceMode, Long companyId);

    CostItemDto calculateUnitPart(Long partId, String priceMode, Double quantity);

    byte[] downloadCostExcel(Long partId, String priceMode, Long companyId) throws IOException;
}
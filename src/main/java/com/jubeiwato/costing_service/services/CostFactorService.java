package com.jubeiwato.costing_service.services;

import java.util.List;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;

public interface CostFactorService {
    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int pageNo, int pageSize, Long companyId);
    
    void createCostFactor(CostFactorDto dto, Long companyId);
        
    CostFactorDto updateCostFactor(Long id, CostFactorDto dto, Long companyId);

    GeneralResponseDto deleteCostFactor( Long id, Long companyId);


}

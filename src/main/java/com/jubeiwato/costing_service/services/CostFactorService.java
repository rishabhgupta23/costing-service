package com.jubeiwato.costing_service.services;

import java.util.List;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;

public interface CostFactorService {
    ApiPageResponseDto<List<CostFactorDto>> getCostFactors(
            int pageNo,
            int pageSize,
            Long companyId,
            String factorName,
            String sortColumn,
            Sorting sortMode
    );

    void createCostFactor(CostFactorDto request, Long companyId);

    CostFactorDto updateCostFactor(
            Long id,
            CostFactorDto request,
            Long companyId
    );

    void deleteCostFactor(Long id, Long companyId);
}

package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PartDataDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.dtos.PartUnitDto;

public interface PartService {
    
    public ApiPageResponseDto<PartDataDto> getParts(int page, int size);

    public List<String> getPartTypes();

    public ApiPageResponseDto<List<PartUnitDto>> getPartUnits(int page, int size);

    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int page, int size);

    public void createPart(PartRequestDto request);


    public PartDto getPartById(Long partId);

    public PartDto updatePartById(Long partId, String partName, PartType type, String unit,
            String categoryName);

    void deletePartById(Long partId);

}

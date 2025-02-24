package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PartDataDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.dtos.PartUnitDto;
import com.jubeiwato.costing_service.entities.Part;

public interface PartService {
    
    public ApiPageResponseDto<PartDataDto> getParts(int pageNo, int pageSize);

    public List<String> getPartTypes();

    public ApiPageResponseDto<List<PartUnitDto>> getPartUnits(int pageNo, int pageSize);

    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int pageNo, int pageSize);

    public void createPart(PartRequestDto request);


    public PartDto getPartById(Long partId);

    public PartDto updatePartById(Long partId, PartRequestDto request);

    void deletePartById(Long partId);

    public List<Part> getFilteredParts(Part filter);

}

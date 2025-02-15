package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PartDataDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;

public interface PartService {

    public ApiPageResponseDto<PartDataDto> getParts(int page, int size);

    public List<String> getPartTypes();

    public List<String> getPartUnits();

    public List<CostFactorDto> getCostFactors();

    public void createPart(PartRequestDto request);


    public PartDto getPartById(Long partId);

    public PartDto updatePartById(Long partId, String partName, PartType type, PartUnit unit,
            String categoryName);

    void deletePartById(Long partId);

}

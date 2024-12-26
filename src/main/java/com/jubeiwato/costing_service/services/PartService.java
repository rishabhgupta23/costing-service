package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;

public interface PartService {

    public ApiPageResponseDto<PartDto> getParts(int page, int size);

    public List<String> getPartTypes();

    public List<String> getPartUnits();

    public List<CostFactorDto> getCostFactors();

    public void createPart(PartRequestDto request);
}

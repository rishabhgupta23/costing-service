package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.entities.Part;
import java.io.IOException;

public interface PartService {

    public ApiPageResponseDto<PartDataDto> getParts(Part filter, int pageNo, int pageSize, String sortBy, Sorting sortDir);

    public List<String> getPartTypes();

    public ApiPageResponseDto<List<PartUnitDto>> getPartUnits(int pageNo, int pageSize);

    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int pageNo, int pageSize);

    public void createPart(PartRequestDto request);


    public PartDto getPartById(Long partId);

    public PartDto updatePartById(Long partId, PartRequestDto request);

    void deletePartById(Long partId);

    public CostHistoryResponseDto getPartCostsByPartAndVendor(Long partId, Long vendorId);
    byte[] downloadPartsToExcel() throws IOException;

}

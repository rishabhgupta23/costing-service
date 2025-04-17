package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.*;
import java.io.IOException;

public interface PartService {

    public ApiPageResponseDto<PartDataDto> getParts(PartDto filter,long companyId, int pageNo, int pageSize, String sortBy, Sorting sortDir);

    public List<String> getPartTypes();

    public ApiPageResponseDto<List<PartUnitDto>> getPartUnits(int pageNo, int pageSize);

    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int pageNo, int pageSize, Long companyId);

    public void createPart(PartRequestDto request,Long companyId);


    public PartDto getPartById(Long partId, Long companyId);

    public PartDto updatePartById(Long partId, PartRequestDto request, Long companyId);

    void deletePartById(Long partId, Long companyId);

    public CostHistoryResponseDto getPartCostsByPartAndVendor(Long partId, Long vendorId, Long companyId );
    byte[] downloadPartsToExcel(Long companyId) throws IOException;

    FileResponseDto downloadBomPartListToExcel(Long parentPartId,Long companyId) throws IOException;
}

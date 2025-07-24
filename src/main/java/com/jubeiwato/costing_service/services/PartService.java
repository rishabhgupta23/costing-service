package com.jubeiwato.costing_service.services;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.*;
import java.io.IOException;

public interface PartService {

    public ApiPageResponseDto<PartDataDto> getParts(PartDto filter,long companyId, int pageNo, int pageSize, String sortBy, Sorting sortDir);

    public List<String> getPartTypes();

    public ApiPageResponseDto<List<PartUnitDto>> getPartUnits(int pageNo, int pageSize);

    public PartDto createPart(PartRequestDto request,Long companyId);


    public PartDto getPartById(Long partId, Long companyId);

    public PartDto updatePartById(Long partId, PartRequestDto request, Long companyId);

    void deletePartById(Long partId, Long companyId);

    public CostHistoryResponseDto getPartCostsByPartAndVendor(Long partId, Long vendorId, Long companyId );

    byte[] downloadPartsToExcel(Long companyId) throws IOException;

    FileResponseDto downloadBomPartListToExcel(Long parentPartId,Long companyId) throws IOException;

    String uploadPartFile(Long partId, PartFileUploadDto partFileUploadDto, Long companyId) throws DataIntegrityViolationException, IOException;

    public FileResponseDto downloadFileFromS3(String fileUrl, Long companyId);
    
    List<String> getPartFileUrls(Long partId, Long companyId);

    public void deletePartFile(Long partId, String s3FileKey, Long companyId);

}

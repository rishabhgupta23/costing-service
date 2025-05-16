package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.entities.PartAttribute;

import java.util.List;

public interface PartAttributeService {
    PartAttribute createPartAttribute(PartAttributeDto dto, Long companyId);

    ApiPageResponseDto<List<PartAttributeDto>> getPartAttributeList(Long companyId, String attributeName, int pageNo,
            int pageSize, String sortColumn, Sorting sortMode);

    PartAttribute getPartAttributeById(Long attributeId, Long companyId);

    PartAttribute updatePartAttribute(Long attributeId, PartAttributeDto dto, Long companyId);

    void deletePartAttribute(Long attributeId, Long companyId);
}

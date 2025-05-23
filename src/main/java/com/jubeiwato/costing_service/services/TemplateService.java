package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.TemplateRequestDto;
import com.jubeiwato.costing_service.dtos.TemplateResponseDto;

import java.util.List;

public interface TemplateService {
    void createTemplate(TemplateRequestDto dto, Long companyId);
    public TemplateResponseDto updateTemplate(Long templateId, TemplateRequestDto dto, Long companyId);
    public ApiPageResponseDto<List<TemplateResponseDto>> getAllTemplates(Long companyId, String name, int pageNo,
    int pageSize, String sortColumn, Sorting sortMode);
    TemplateResponseDto getTemplateById(Long id, Long companyId);
    public void deleteTemplate(Long templateId, Long companyId);
}
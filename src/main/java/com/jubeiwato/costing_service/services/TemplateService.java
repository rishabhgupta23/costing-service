package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.TemplateDto;
import com.jubeiwato.costing_service.dtos.TemplateRequestDto;
import com.jubeiwato.costing_service.dtos.TemplateResponseDto;

import java.util.List;

public interface TemplateService {
    void createTemplate(TemplateRequestDto dto, Long companyId);
    public TemplateResponseDto updateTemplate(Long templateId, TemplateRequestDto dto, Long companyId);
    List<TemplateDto> getAllTemplates(Long companyId);
    TemplateResponseDto getTemplateById(Long id, Long companyId);
    public void deleteTemplate(Long templateId, Long companyId);
}
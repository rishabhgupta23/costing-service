package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.TemplateDto;
import com.jubeiwato.costing_service.dtos.TemplateRequestDto;

import java.util.List;

public interface TemplateService {
    void createTemplate(TemplateRequestDto dto, Long companyId);
//     TemplateDto updateTemplate(Long templateId, TemplateDto dto, Long companyId);
//     void deleteTemplate(Long templateId, Long companyId);
//     List<TemplateDto> getTemplates(Long companyId);
//     TemplateDto getTemplateById(Long templateId, Long companyId);
}
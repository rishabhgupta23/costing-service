package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.dtos.TemplateDto;
import com.jubeiwato.costing_service.dtos.TemplateRequestDto;
import com.jubeiwato.costing_service.dtos.TemplateResponseDto;
import com.jubeiwato.costing_service.entities.*;
import com.jubeiwato.costing_service.repositories.*;
import com.jubeiwato.costing_service.services.TemplateService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;
    private final PartAttributeRepository partAttributeRepository;
    private final CompanyRepository companyRepository;
    private final TemplatePartAttributeRepository templatePartAttributeRepository;

    @Override
    @Transactional
    public void createTemplate(TemplateRequestDto dto, Long companyId) {
        Company company = getValidatedCompany(companyId);

        checkIfTemplateNameExists(dto.getName(), company);

        Template temp = Template.builder()
                .name(dto.getName())
                .company(company)
                .build();
                
        Template savedTemplate = templateRepository.save(temp);

        List<PartAttribute> partAttributes = getValidPartAttributes(dto.getPartAttributes(), company);

        List<Template_PartAttribute> mappings = partAttributes.stream()
                .map(attr -> Template_PartAttribute.builder()
                        .template(savedTemplate)
                        .partAttribute(attr)
                        .build())
                .toList();

        templatePartAttributeRepository.saveAll(mappings);
    }

    private void checkIfTemplateNameExists(String name, Company company) {
        boolean exists = templateRepository.existsByNameIgnoreCaseAndCompany(name.trim(), company);
        if (exists) {
                throw new AppException(ErrorMessageConstant.TEMPLATE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
    }

    private List<PartAttribute> getValidPartAttributes(List<Long> dtoList, Company company) {
        List<Long> distinctIds = dtoList.stream()
                .distinct()
                .toList();

        List<PartAttribute> foundAttributes = partAttributeRepository
                .findByAttributeIdInAndCompany(distinctIds, company);

        Set<Long> foundIds = foundAttributes.stream()
                .map(PartAttribute::getAttributeId)
                .collect(Collectors.toSet());

        List<Long> missingAttributes = distinctIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingAttributes.isEmpty()) {
                throw new AppException(
                        ErrorMessageConstant.ATTRIBUTE_NOT_FOUND + missingAttributes,
                        HttpStatus.BAD_REQUEST
                    );
        }
    
        return foundAttributes;
    } 

    private Company getValidatedCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    public List<TemplateDto> getAllTemplates(Long companyId) {
        Company company = getValidatedCompany(companyId);

        return templateRepository.findByCompany(company).stream()
                .map(template -> TemplateDto.builder()
                        .templateId(template.getTemplateId())
                        .name(template.getName())
                        .build())
                .toList();
    }

@Override
public TemplateResponseDto getTemplateById(Long templateId, Long companyId) {
        Company company = getValidatedCompany(companyId);

        Template template = templateRepository.findByTemplateIdAndCompany(templateId, company)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.TEMPLATE_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<PartAttributeDto> partAttributes = templatePartAttributeRepository.findByTemplate(template).stream()
                .map(rel -> PartAttributeDto.entityToDto(rel.getPartAttribute()))
                .toList();

        return TemplateResponseDto.builder()
                .templateId(template.getTemplateId())
                .name(template.getName())
                .partAttributes(partAttributes)
                .build();
    }

    @Override
@Transactional
public TemplateResponseDto updateTemplate(Long templateId, TemplateRequestDto dto, Long companyId) {
    Company company = getValidatedCompany(companyId);

    Template template = templateRepository.findByTemplateIdAndCompany(templateId, company)
            .orElseThrow(() -> new AppException(ErrorMessageConstant.TEMPLATE_NOT_FOUND, HttpStatus.NOT_FOUND));

    String newName = dto.getName().trim();
    if (!template.getName().equalsIgnoreCase(newName)) {
        checkIfTemplateNameExists(newName, company);
        template.setName(newName);
    }

    List<PartAttribute> validAttributes = getValidPartAttributes(dto.getPartAttributes(), company);

    templatePartAttributeRepository.deleteByTemplate(template);

    List<Template_PartAttribute> newMappings = validAttributes.stream()
            .map(attr -> Template_PartAttribute.builder()
                    .template(template)
                    .partAttribute(attr)
                    .build())
            .toList();
    templatePartAttributeRepository.saveAll(newMappings);

    Template updated = templateRepository.save(template);

    List<PartAttributeDto> partAttributes = templatePartAttributeRepository.findByTemplate(template).stream()
    .map(rel -> PartAttributeDto.entityToDto(rel.getPartAttribute()))
    .toList();
    return TemplateResponseDto.builder()
            .templateId(updated.getTemplateId())
            .name(updated.getName())
            .partAttributes(partAttributes)
            .build();
}


@Override
@Transactional
public void deleteTemplate(Long templateId, Long companyId) {
    Company company = getValidatedCompany(companyId);

    Template template = templateRepository.findByTemplateIdAndCompany(templateId, company)
            .orElseThrow(() -> new AppException(ErrorMessageConstant.TEMPLATE_NOT_FOUND, HttpStatus.NOT_FOUND));

    templatePartAttributeRepository.deleteByTemplate(template);
    templateRepository.delete(template);
}

}

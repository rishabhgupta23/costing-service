package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.dtos.TemplateRequestDto;
import com.jubeiwato.costing_service.dtos.TemplateResponseDto;
import com.jubeiwato.costing_service.entities.*;
import com.jubeiwato.costing_service.repositories.*;
import com.jubeiwato.costing_service.services.TemplateService;
import com.jubeiwato.costing_service.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    private void validateTemplateInput(TemplateRequestDto dto) {
        if (dto.getTemplateName() == null || dto.getTemplateName().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.TEMPLATE_NULL, HttpStatus.BAD_REQUEST);
        }
        if (dto.getPartAttributes() == null || dto.getPartAttributes().isEmpty()) {
            throw new AppException(ErrorMessageConstant.ATTRIBUTE_NOT_SELECTED, HttpStatus.BAD_REQUEST);
        }
    }
    @Override
    @Transactional
    public void createTemplate(TemplateRequestDto dto, Long companyId) {

        validateTemplateInput(dto);
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new AppException(
                            ErrorMessageConstant.INVALID_COMPANY,
                            HttpStatus.BAD_REQUEST));
        checkIfTemplateNameExists(dto.getTemplateName(), company);

        Template temp = Template.builder()
                .templateName(dto.getTemplateName())
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
        boolean exists = templateRepository.existsByTemplateNameIgnoreCaseAndCompany(name.trim(), company);
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

    @Override
    public ApiPageResponseDto<List<TemplateResponseDto>> getAllTemplates(Long companyId, String name, int pageNo,
    int pageSize, String sortColumn, Sorting sortMode) {
        if (!ValidationUtil.isValidInput(name)) {
                throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
            }
    
            Sort.Direction direction = (sortMode == Sorting.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortColumn);
            Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
                Specification<Template> spec = new TemplateSpecification(companyId, name);


            Page<Template> templatePage = templateRepository.findAll(spec, pageable);

            List<TemplateResponseDto> dtoList = templatePage.getContent()
            .stream()
            .map(TemplateResponseDto::entityToDto)
            .toList();

    PageInfoDto pageInfo = PageInfoDto.builder()
            .totalPages(templatePage.getTotalPages())
            .pageNumber(pageNo)
            .pageSize(pageSize)
            .totalRecords(templatePage.getTotalElements())
            .build();

    return ApiPageResponseDto.<List<TemplateResponseDto>>builder()
            .data(dtoList)
            .pageInfo(pageInfo)
            .build();
}
private Template getValidatedTemplate(Long templateId, Long companyId) {
        return templateRepository.findByTemplateIdAndCompany_CompanyId(templateId, companyId)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.TEMPLATE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));
    }
@Override
public TemplateResponseDto getTemplateById(Long templateId, Long companyId) {

        Template template = getValidatedTemplate(templateId, companyId);

        List<PartAttributeDto> partAttributes = templatePartAttributeRepository.findByTemplate(template).stream()
                .map(rel -> PartAttributeDto.entityToDto(rel.getPartAttribute()))
                .toList();

        return TemplateResponseDto.builder()
                .templateId(template.getTemplateId())
                .templateName(template.getTemplateName())
                .partAttributes(partAttributes)
                .build();
    }

    @Override
@Transactional
public TemplateResponseDto updateTemplate(Long templateId, TemplateRequestDto dto, Long companyId) {

        validateTemplateInput(dto);


        Template template = getValidatedTemplate(templateId, companyId);
    String newName = dto.getTemplateName().trim();
    if (!template.getTemplateName().equalsIgnoreCase(newName)) {
        checkIfTemplateNameExists(newName, template.getCompany());
        template.setTemplateName(newName);
    }

    List<PartAttribute> validAttributes = getValidPartAttributes(dto.getPartAttributes(),template.getCompany());

    templatePartAttributeRepository.deleteByTemplate(template);

    List<Template_PartAttribute> newMappings = validAttributes.stream()
            .map(attr -> Template_PartAttribute.builder()
                    .template(template)
                    .partAttribute(attr)
                    .build())
            .toList();
    templatePartAttributeRepository.saveAll(newMappings);

    Template updated = templateRepository.save(template);

    List<PartAttributeDto> partAttributes = validAttributes.stream()
    .map(PartAttributeDto::entityToDto)
    .toList();
    return TemplateResponseDto.builder()
            .templateId(updated.getTemplateId())
            .templateName(updated.getTemplateName())
            .partAttributes(partAttributes)
            .build();
}


@Override
@Transactional
public void deleteTemplate(Long templateId, Long companyId) {

        Template template = getValidatedTemplate(templateId, companyId);

    templatePartAttributeRepository.deleteByTemplate(template);
    templateRepository.delete(template);
}

}

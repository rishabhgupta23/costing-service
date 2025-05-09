package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.dtos.TemplateRequestDto;
import com.jubeiwato.costing_service.entities.*;
import com.jubeiwato.costing_service.repositories.*;
import com.jubeiwato.costing_service.services.TemplateService;

import lombok.RequiredArgsConstructor;
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
        Company company = getCompany(companyId);

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
            throw new IllegalArgumentException("Template name already exists for the company.");
        }
    }

    private List<PartAttribute> getValidPartAttributes(List<PartAttributeDto> dtoList, Company company) {
        List<String> names = dtoList.stream()
                .map(attr -> attr.getName().trim())
                .distinct()
                .toList();

        List<PartAttribute> foundAttributes = partAttributeRepository
                .findByNameInIgnoreCaseAndCompany(names, company);

        Set<String> foundNames = foundAttributes.stream()
                .map(attr -> attr.getName().toLowerCase())
                .collect(Collectors.toSet());

        List<String> missingNames = names.stream()
                .filter(name -> !foundNames.contains(name.toLowerCase()))
                .toList();

        if (!missingNames.isEmpty()) {
            throw new IllegalArgumentException("Attributes not found for company: " + missingNames);
        }

        return foundAttributes;
    }

    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
    }
}

package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.PartAttribute;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.PartAttributeRepository;
import com.jubeiwato.costing_service.services.PartAttributeService;
import com.jubeiwato.costing_service.services.PartAttributeSpecification;
import com.jubeiwato.costing_service.utils.ValidationUtil;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartAttributeServiceImpl implements PartAttributeService {
    @Autowired
    private PartAttributeRepository partAttributeRepository;

    private final CompanyRepository companyRepository;

    private PartAttribute getValidatedPartAttribute(Long attributeId, Long companyId) {
        return partAttributeRepository
                .findByAttributeIdAndCompany_CompanyId(attributeId, companyId)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.ATTRIBUTE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));
    }

    @Override
    public PartAttribute createPartAttribute(PartAttributeDto partAttributeDto, Long companyId) {

        if (partAttributeDto.getAttributeName() == null || partAttributeDto.getAttributeName().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.PART_ATTRIBUTE_NOT_NULL, HttpStatus.BAD_REQUEST);
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.INVALID_COMPANY,
                        HttpStatus.BAD_REQUEST));

        Optional<PartAttribute> existing = partAttributeRepository
                .findByAttributeNameAndCompany_CompanyId(partAttributeDto.getAttributeName(), companyId);

        if (existing.isPresent()) {
            PartAttribute existingAttribute = existing.get();
            if (existingAttribute.getDeleteFlag().equals(DeleteFlag.NEGATIVE.getValue())) {

                throw new AppException(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, HttpStatus.CONFLICT);
            } else {

                existingAttribute.setDeleteFlag(DeleteFlag.NEGATIVE.getValue());
                return partAttributeRepository.save(existingAttribute);
            }
        }

        PartAttribute partAttribute = PartAttribute.builder()
                .attributeName(partAttributeDto.getAttributeName())
                .company(company)
                .build();

        return partAttributeRepository.save(partAttribute);
    }

    @Override
    public ApiPageResponseDto<List<PartAttributeDto>> getPartAttributeList(Long companyId, String attributeName,
            int pageNo,
            int pageSize, String sortColumn, Sorting sortMode) {

        if (!ValidationUtil.isValidInput(attributeName)) {
            throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        Sort.Direction direction = (sortMode == Sorting.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortColumn);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<PartAttribute> spec = new PartAttributeSpecification(companyId, attributeName, 0);

        Page<PartAttribute> partAttributePage = partAttributeRepository.findAll(spec, pageable);

        List<PartAttributeDto> dtoList = partAttributePage.getContent()
                .stream()
                .map(PartAttributeDto::entityToDto)
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
                .totalPages(partAttributePage.getTotalPages())
                .pageNumber(pageNo)
                .pageSize(pageSize)
                .totalRecords(partAttributePage.getTotalElements())
                .build();

        return ApiPageResponseDto.<List<PartAttributeDto>>builder()
                .data(dtoList)
                .pageInfo(pageInfo)
                .build();
    }

    @Override
    public PartAttribute getPartAttributeById(Long attributeId, Long companyId) {
        return getValidatedPartAttribute(attributeId, companyId);
    }

    @Override
    public PartAttribute updatePartAttribute(Long attributeId, PartAttributeDto partAttributeDto, Long companyId) {

        PartAttribute existing = getValidatedPartAttribute(attributeId, companyId);

        if (existing.getDeleteFlag().equals(DeleteFlag.POSITIVE.getValue())) {
            throw new AppException(ErrorMessageConstant.ATTRIBUTE_MARKED_DELETED, HttpStatus.BAD_REQUEST);
        }

        if (partAttributeDto.getAttributeName() == null || partAttributeDto.getAttributeName().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.PART_ATTRIBUTE_NOT_NULL, HttpStatus.BAD_REQUEST);
        }

        String newName = partAttributeDto.getAttributeName();

        if (!existing.getAttributeName().equalsIgnoreCase(newName)) {

            // Check if active attribute with the new name exists
            Optional<PartAttribute> activeAttribute = partAttributeRepository
                    .findByAttributeNameAndCompany_CompanyIdAndDeleteFlag(newName, companyId,
                            DeleteFlag.NEGATIVE.getValue());

            if (activeAttribute.isPresent()) {
                // Active attribute exists with that name - conflict
                throw new AppException(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, HttpStatus.CONFLICT);
            }

            Optional<PartAttribute> softDeletedAttribute = partAttributeRepository
                    .findByAttributeNameAndCompany_CompanyIdAndDeleteFlag(newName, companyId,
                            DeleteFlag.POSITIVE.getValue());

            if (softDeletedAttribute.isPresent()) {
                String errorMsg = String.format(ErrorMessageConstant.UPDATE_NOT_ALLOWED_SOFT_DELETED, newName);
                throw new AppException(errorMsg, HttpStatus.BAD_REQUEST);
            }

            existing.setAttributeName(newName);
        }

        return partAttributeRepository.save(existing);
    }

    @Override
    public void deletePartAttribute(Long attributeId, Long companyId) {

        PartAttribute existing = getValidatedPartAttribute(attributeId, companyId);

        if (existing.getDeleteFlag() != null && existing.getDeleteFlag().equals(DeleteFlag.POSITIVE.getValue())) {
            throw new AppException(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        existing.setDeleteFlag(DeleteFlag.POSITIVE.getValue());
        partAttributeRepository.save(existing);

    }

}

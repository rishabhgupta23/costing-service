package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.PartAttribute;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.PartAttributeRepository;
import com.jubeiwato.costing_service.services.PartAttributeService;
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
        return partAttributeRepository.findAllByAttributeIdAndCompany_CompanyIdAndDeleteFlag(attributeId, companyId, 0)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.ATTRIBUTE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));
    }

    @Override
    public PartAttribute createPartAttribute(PartAttributeDto partAttributeDto, Long companyId) {

        if (partAttributeDto.getName() == null || partAttributeDto.getName().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.PARTATTRIBUTE_MUST_BE_NOTNULL, HttpStatus.BAD_REQUEST);
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.INVALID_COMPANY,
                        HttpStatus.BAD_REQUEST));

        Optional<PartAttribute> existing = partAttributeRepository
                .findByNameAndCompany_CompanyId(partAttributeDto.getName(), companyId);

        if (existing.isPresent()) {
            PartAttribute existingAttribute = existing.get();
            if (existingAttribute.getDeleteFlag() == 0) {
                // Attribute exists and is active
                throw new AppException(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, HttpStatus.CONFLICT);
            } else {
                // Attribute exists but is soft-deleted — reactivate it
                existingAttribute.setDeleteFlag(0);
                return partAttributeRepository.save(existingAttribute);
            }
        }

        PartAttribute partAttribute = PartAttribute.builder()
                .name(partAttributeDto.getName())
                .company(company)
                .build();

        return partAttributeRepository.save(partAttribute);
    }

    @Override
    public ApiPageResponseDto<List<PartAttributeDto>> getPartAttributeList(Long companyId, String name, int pageNo,
            int pageSize, String sortColumn, Sorting sortMode) {

        if (!ValidationUtil.isValidInput(name)) {
            throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        Sort.Direction direction = (sortMode == Sorting.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortColumn);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<PartAttribute> spec = PartAttributeSpecification.getFilteredPartAttributes(companyId, name);

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

        if (existing.getDeleteFlag() == 1) {
            throw new AppException(ErrorMessageConstant.ATTRIBUTE_MARKED_DELETED, HttpStatus.BAD_REQUEST);
        }

        if (partAttributeDto.getName() == null || partAttributeDto.getName().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.PARTATTRIBUTE_MUST_BE_NOTNULL, HttpStatus.BAD_REQUEST);
        }

        if (!existing.getCompany().getCompanyId().equals(companyId)) {
            throw new AppException(ErrorMessageConstant.INVALID_COMPANY, HttpStatus.BAD_REQUEST);
        }
        Optional<PartAttribute> name = partAttributeRepository
                .findByNameAndCompany_CompanyIdAndDeleteFlag(partAttributeDto.getName(), companyId, 0);

        if (name.isPresent() && !name.get().getAttributeId().equals(attributeId)) {
            throw new AppException(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        existing.setName(partAttributeDto.getName());

        return partAttributeRepository.save(existing);
    }

    @Override
    public GeneralResponseDto deletePartAttribute(Long attributeId, Long companyId) {

        PartAttribute existing = partAttributeRepository.findByAttributeIdAndCompany_CompanyId(attributeId, companyId)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (existing.getDeleteFlag() != null && existing.getDeleteFlag() == 1) {
            throw new AppException(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        Optional<PartAttribute> duplicateSoftDeleted = partAttributeRepository
                .findByNameAndCompany_CompanyIdAndDeleteFlag(existing.getName(), companyId, 1);

        duplicateSoftDeleted.ifPresent(partAttributeRepository::delete);

        existing.setDeleteFlag(1);
        partAttributeRepository.save(existing);

        return GeneralResponseDto.builder()
                .message("Part attribute deleted successfully.")
                .status(HttpStatus.OK.value())
                .build();
    }

}

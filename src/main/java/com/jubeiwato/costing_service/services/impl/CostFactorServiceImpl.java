package com.jubeiwato.costing_service.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.jubeiwato.costing_service.utils.SearchUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.CostFactor;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.CostFactorRepository;
import com.jubeiwato.costing_service.services.CostFactorService;
import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.services.CostFactorSpecification;
import com.jubeiwato.costing_service.utils.ValidationUtil;

@Service
public class CostFactorServiceImpl implements CostFactorService {

    private final CostFactorRepository costFactorRepository;
    private final CompanyRepository companyRepository;

    public CostFactorServiceImpl(
            CostFactorRepository costFactorRepository,
            CompanyRepository companyRepository) {

        this.costFactorRepository = costFactorRepository;
        this.companyRepository = companyRepository;
    }

    private CostFactor getValidatedCostFactor(Long id, Long companyId) {

        return costFactorRepository
                .findByFactorIdAndCompany_CompanyIdAndDeleteFlag(
                        id,
                        companyId,
                        DeleteFlag.NEGATIVE.getValue()
                )
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST,
                        HttpStatus.NOT_FOUND
                ));
    }

    private Optional<CostFactor> getConflictingCostFactor(
            String factorName,
            Long companyId) {

        String trimmedName =
                factorName != null ? factorName.trim() : "";

        if (trimmedName.isEmpty()) {
            throw new AppException(
                    ErrorMessageConstant.INVALID_COST_FACTOR,
                    HttpStatus.BAD_REQUEST
            );
        }

        return costFactorRepository
                .findByCompany_CompanyIdAndFactorNameIgnoreCase(
                        companyId,
                        trimmedName
                );
    }

    @Override
    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(
            int pageNo,
            int pageSize,
            Long companyId,
            String factorName,
            String sortColumn,
            Sorting sortMode) {

        if (!ValidationUtil.isValidInput(factorName)) {
            throw new AppException(
                    ErrorMessageConstant.INVALID_INPUT,
                    HttpStatus.BAD_REQUEST
            );
        }

        Specification<CostFactor> spec =
                new CostFactorSpecification(
                        companyId,
                        factorName,
                        DeleteFlag.NEGATIVE.getValue()
                );

        boolean hasSearch =
                SearchUtil.hasSearchCriteria(factorName);

        Pageable pageable;

        if (hasSearch) {

            /*
             * When searching, do not apply the normal sorting.
             * SearchUtil/Specification handles the search ordering.
             */
            pageable = PageRequest.of(
                    pageNo,
                    pageSize
            );

        } else {

            Sort.Direction direction =
                    sortMode == Sorting.DESC
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;

            Sort sort = Sort.by(
                    direction,
                    sortColumn
            );

            pageable = PageRequest.of(
                    pageNo,
                    pageSize,
                    sort
            );
        }

        Page<CostFactor> costFactorPage =
                costFactorRepository.findAll(
                        spec,
                        pageable
                );

        List<CostFactorDto> costFactorDtos =
                costFactorPage.getContent()
                        .stream()
                        .map(costFactor ->
                                CostFactorDto.builder()
                                        .id(costFactor.getFactorId())
                                        .factorName(costFactor.getFactorName())
                                        .factorType(costFactor.getFactorType())
                                        .build()
                        )
                        .toList();

        PageInfoDto pageInfo =
                PageInfoDto.builder()
                        .totalPages(costFactorPage.getTotalPages())
                        .pageNumber(pageNo)
                        .pageSize(pageSize)
                        .totalRecords(costFactorPage.getTotalElements())
                        .build();

        return ApiPageResponseDto
                .<List<CostFactorDto>>builder()
                .data(costFactorDtos)
                .pageInfo(pageInfo)
                .build();
    }

    @Override
    public void createCostFactor(
            CostFactorDto request,
            Long companyId) {

        Company company =
                companyRepository.findById(companyId)
                        .orElseThrow(() -> new AppException(
                                ErrorMessageConstant.INVALID_COMPANY,
                                HttpStatus.BAD_REQUEST
                        ));

        String trimmedName =
                validateCostFactorRequest(request);

        Optional<CostFactor> conflictOpt =
                getConflictingCostFactor(
                        trimmedName,
                        companyId
                );

        if (conflictOpt.isPresent()) {

            CostFactor existing = conflictOpt.get();

            /*
             * Active cost factor already exists.
             */
            if (Objects.equals(
                    existing.getDeleteFlag(),
                    DeleteFlag.NEGATIVE.getValue())) {

                throw new AppException(
                        ErrorMessageConstant.getFormattedMessage(
                                ErrorMessageConstant
                                        .COST_FACTOR_ALREADY_EXISTS_TEMPLATE,
                                trimmedName
                        ),
                        HttpStatus.CONFLICT
                );
            }

            /*
             * Restore soft-deleted cost factor.
             */
            existing.setDeleteFlag(
                    DeleteFlag.NEGATIVE.getValue()
            );

            existing.setFactorName(trimmedName);

            existing.setFactorType(
                    request.getFactorType()
            );

            costFactorRepository.save(existing);

            return;
        }

        /*
         * Create new cost factor.
         */
        CostFactor costFactor =
                CostFactor.builder()
                        .factorName(trimmedName)
                        .factorType(request.getFactorType())
                        .company(company)
                        .build();

        costFactorRepository.save(costFactor);
    }

    @Override
    public CostFactorDto updateCostFactor(
            Long id,
            CostFactorDto request,
            Long companyId) {

        CostFactor existing =
                getValidatedCostFactor(
                        id,
                        companyId
                );

        String trimmedName =
                validateCostFactorRequest(request);

        /*
         * Check whether the name itself is changing.
         */
        if (!existing.getFactorName()
                .equalsIgnoreCase(trimmedName)) {

            Optional<CostFactor> conflictOpt =
                    getConflictingCostFactor(
                            trimmedName,
                            companyId
                    );

            /*
             * Ignore the current record while checking
             * for duplicate names.
             */
            if (conflictOpt.isPresent()
                    && !Objects.equals(
                    conflictOpt.get().getFactorId(),
                    existing.getFactorId())) {

                CostFactor conflict =
                        conflictOpt.get();

                String message;

                /*
                 * The requested name belongs to a
                 * soft-deleted cost factor.
                 */
                if (Objects.equals(
                        conflict.getDeleteFlag(),
                        DeleteFlag.POSITIVE.getValue())) {

                    message =
                            ErrorMessageConstant.getFormattedMessage(
                                    ErrorMessageConstant
                                            .COST_FACTOR_SOFT_DELETED_EXISTS_TEMPLATE,
                                    trimmedName
                            );

                } else {

                    /*
                     * The requested name already belongs
                     * to an active cost factor.
                     */
                    message =
                            ErrorMessageConstant.getFormattedMessage(
                                    ErrorMessageConstant
                                            .COST_FACTOR_ALREADY_EXISTS_TEMPLATE,
                                    trimmedName
                            );
                }

                throw new AppException(
                        message,
                        HttpStatus.CONFLICT
                );
            }

            existing.setFactorName(trimmedName);
        }

        /*
         * Update factor type.
         */
        existing.setFactorType(
                request.getFactorType()
        );

        CostFactor saved =
                costFactorRepository.save(existing);

        return CostFactorDto.entityToDto(
                saved,
                null,
                null,
                null,
                null
        );
    }

    private String validateCostFactorRequest(
            CostFactorDto request) {

        if (request == null) {
            throw new AppException(
                    ErrorMessageConstant.INVALID_COST_FACTOR,
                    HttpStatus.BAD_REQUEST
            );
        }

        String trimmedName =
                request.getFactorName() != null
                        ? request.getFactorName().trim()
                        : "";

        if (trimmedName.isEmpty()) {
            throw new AppException(
                    ErrorMessageConstant.INVALID_COST_FACTOR,
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getFactorType() == null) {
            throw new AppException(
                    "Cost factor type is required",
                    HttpStatus.BAD_REQUEST
            );
        }

        return trimmedName;
    }

    @Override
    public void deleteCostFactor(
            Long id,
            Long companyId) {

        CostFactor existing =
                getValidatedCostFactor(
                        id,
                        companyId
                );

        existing.setDeleteFlag(
                DeleteFlag.POSITIVE.getValue()
        );

        costFactorRepository.save(existing);
    }
}
package com.jubeiwato.costing_service.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
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

    public CostFactorServiceImpl(CostFactorRepository costFactorRepository, CompanyRepository companyRepository) {
        this.costFactorRepository = costFactorRepository;
        this.companyRepository = companyRepository;
    }

    private CostFactor getValidatedCostFactor(Long id, Long companyId) {
        return costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(id, companyId, DeleteFlag.NEGATIVE.getValue())
                .orElseThrow(() -> new AppException(ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));
    }

    private List<CostFactor> findExactMatchInCompany(String factorName, Long companyId) {
        String trimmedName = (factorName != null) ? factorName.trim() : "";
        if (trimmedName.isEmpty()) {
            throw new AppException(ErrorMessageConstant.INVALID_COST_FACTOR, HttpStatus.BAD_REQUEST);
        }
        return costFactorRepository.findByCompany_CompanyIdAndFactorNameIgnoreCase(companyId, trimmedName);
    }

    private Optional<CostFactor> findConflictingCostFactor(String factorName, Long companyId) {
        String trimmedName = (factorName != null) ? factorName.trim() : "";

        if (trimmedName.isEmpty()) {
            throw new AppException(ErrorMessageConstant.INVALID_COST_FACTOR, HttpStatus.BAD_REQUEST);
        }
        List<CostFactor> matches = costFactorRepository.findByCompany_CompanyIdAndFactorNameIgnoreCase(companyId, trimmedName);
        Optional<CostFactor> softDeleted = Optional.empty();

        for (CostFactor conflict : matches) {

            if (Objects.equals(conflict.getDeleteFlag(), DeleteFlag.NEGATIVE.getValue())) {
                String msg = ErrorMessageConstant.getFormattedMessage(
                        ErrorMessageConstant.COST_FACTOR_ALREADY_EXISTS_TEMPLATE, trimmedName);
                throw new AppException(msg, HttpStatus.CONFLICT);
            } else {
                softDeleted = Optional.of(conflict);
            }
        }
        return softDeleted;
    }

    @Override
    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int pageNo, int pageSize, Long companyId, String factorName, String sortColumn, Sorting sortMode) {
        Sort.Direction direction = (sortMode == Sorting.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortColumn);
        if (!ValidationUtil.isValidInput(factorName)) {
            throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<CostFactor> spec = new CostFactorSpecification(companyId, factorName, DeleteFlag.NEGATIVE.getValue());
        Page<CostFactor> costFactorPage = costFactorRepository.findAll(spec, pageable);

        List<CostFactorDto> costFactorDtos = costFactorPage.getContent()
                .stream()
                .map(costFactor -> CostFactorDto.builder()
                        .id(costFactor.getFactorId())
                        .name(costFactor.getFactorName())
                        .build())
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
                .totalPages(costFactorPage.getTotalPages())
                .pageNumber(pageNo)
                .pageSize(pageSize)
                .totalRecords(costFactorPage.getTotalElements())
                .build();

        return ApiPageResponseDto.<List<CostFactorDto>>builder()
                .data(costFactorDtos)
                .pageInfo(pageInfo)
                .build();
    }



    @Override
    public void createCostFactor(String factorName, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_COMPANY, HttpStatus.BAD_REQUEST));

        Optional<CostFactor> softDeleted = findConflictingCostFactor(factorName, companyId);

        if (softDeleted.isPresent()) {
            CostFactor reactivated = softDeleted.get();
            reactivated.setDeleteFlag(DeleteFlag.NEGATIVE.getValue());
            reactivated.setFactorName(factorName.trim());
            costFactorRepository.save(reactivated);
            throw new AppException(ErrorMessageConstant.COST_FACTOR_CREATED, HttpStatus.OK);
        }

        CostFactor costFactor = CostFactor.builder()
                .factorName(factorName.trim())
                .company(company)
                .build();

        costFactorRepository.save(costFactor);
    }

    @Override
    public CostFactorDto updateCostFactor(Long id, String factorName, Long companyId) {
        CostFactor existing = getValidatedCostFactor(id, companyId);
        String trimmedName = (factorName != null) ? factorName.trim() : "";

        if (trimmedName.isEmpty()) {
            throw new AppException(ErrorMessageConstant.INVALID_COST_FACTOR, HttpStatus.BAD_REQUEST);
        }

        if (!existing.getFactorName().equalsIgnoreCase(trimmedName)) {
            List<CostFactor> matches = findExactMatchInCompany(trimmedName, companyId);

            for (CostFactor conflict : matches) {
                if (conflict.getFactorId() == id) continue;

                if (conflict.getDeleteFlag() == DeleteFlag.NEGATIVE.getValue()) {
                    String msg = ErrorMessageConstant.getFormattedMessage(
                            ErrorMessageConstant.COST_FACTOR_ALREADY_EXISTS_TEMPLATE, trimmedName);
                    throw new AppException(msg, HttpStatus.CONFLICT);
                }
            }

            existing.setFactorName(trimmedName);
        }

        costFactorRepository.save(existing);

        return CostFactorDto.builder()
                .id(existing.getFactorId())
                .name(existing.getFactorName())
                .build();
    }



@Override
public GeneralResponseDto deleteCostFactor(Long id, Long companyId) {
    CostFactor existing = getValidatedCostFactor(id, companyId);
    if (Objects.equals(existing.getDeleteFlag(), DeleteFlag.POSITIVE.getValue())) {
        return GeneralResponseDto.builder()
                .message("Cost factor is already deleted.")
                .status(HttpStatus.OK.value())
                .build();
    }

    existing.setDeleteFlag(DeleteFlag.POSITIVE.getValue());
    costFactorRepository.save(existing);

    return GeneralResponseDto.builder()
            .message("Cost factor deleted successfully.")
            .status(HttpStatus.OK.value())
            .build();
}


}

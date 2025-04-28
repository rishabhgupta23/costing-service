package com.jubeiwato.costing_service.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.CostFactor;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.CostFactorRepository;
import com.jubeiwato.costing_service.services.CostFactorService;

@Service
public class CostFactorServiceImpl implements CostFactorService {

      private final CostFactorRepository costFactorRepository;
    private final CompanyRepository companyRepository;

    public CostFactorServiceImpl(CostFactorRepository costFactorRepository, CompanyRepository companyRepository) {
        this.costFactorRepository = costFactorRepository;
    this.companyRepository = companyRepository;
    } 

    private CostFactor getValidatedCostFactor(Long id, Long companyId) {
        return costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(id, companyId, 0)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));
  }
    
     
  private void validateUniqueCostFactorName(String factorName, Long companyId) {
    boolean exists = costFactorRepository.existsByCompany_CompanyIdAndFactorNameAndDeleteFlag(companyId, factorName, 0);
    if (exists) {
        String msg = ErrorMessageConstant.getFormattedMessage(
                ErrorMessageConstant.COST_FACTOR_ALREADY_EXISTS_TEMPLATE, factorName);
        throw new AppException(msg, HttpStatus.CONFLICT);
    }
}


    @Override
    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int pageNo, int pageSize, Long companyId) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<CostFactor> costFactorPage = costFactorRepository.findByCompany_CompanyIdAndDeleteFlag(companyId, 0, pageable);
    
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
public void createCostFactor(CostFactorDto dto, Long companyId) {

    Company company = companyRepository.findById(companyId)
    .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_COMPANY, HttpStatus.BAD_REQUEST));

        validateUniqueCostFactorName(dto.getName(), companyId);

    CostFactor costFactor = CostFactor.builder()
        .factorName(dto.getName())
        .company(company)
        .build();

    // Save it using the repository
    costFactorRepository.save(costFactor);
}

@Override
public CostFactorDto updateCostFactor(Long id, CostFactorDto dto, Long companyId) {
    CostFactor existing = getValidatedCostFactor(id, companyId);

    if (!existing.getFactorName().equalsIgnoreCase(dto.getName())) {

        boolean exists = costFactorRepository.existsByCompany_CompanyIdAndFactorNameAndDeleteFlag(companyId, dto.getName(), 0);
        if (exists) {
            String msg = ErrorMessageConstant.getFormattedMessage(
                ErrorMessageConstant.COST_FACTOR_ALREADY_EXISTS_TEMPLATE, dto.getName());
            throw new AppException(msg, HttpStatus.CONFLICT);
        }
        existing.setFactorName(dto.getName());
    }

    costFactorRepository.save(existing);

    return CostFactorDto.builder()
            .name(existing.getFactorName())
            .build();
}



@Override
public GeneralResponseDto deleteCostFactor(Long id, Long companyId) {
    CostFactor existing = costFactorRepository.findByFactorIdAndCompany_CompanyId(id, companyId)
            .orElseThrow(() -> new AppException(
                    ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));

    if (existing.getDeleteFlag() != null && existing.getDeleteFlag() == 1) {
        throw new AppException(ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
    }

    existing.setDeleteFlag(1);
    costFactorRepository.save(existing);

    return GeneralResponseDto.builder()
            .message("Cost factor deleted successfully.")
            .status(HttpStatus.OK.value())
            .build();
}


}

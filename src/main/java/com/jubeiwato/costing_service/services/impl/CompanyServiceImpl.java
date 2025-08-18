package com.jubeiwato.costing_service.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.UserRoleConstants;
import com.jubeiwato.costing_service.dtos.CompanyDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.services.CompanyService;
import com.jubeiwato.costing_service.utils.ValidationUtil;
import com.jubeiwato.costing_service.entities.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    
private void validateCompanyEmailUnique(String emailId) {
    Optional<Company> existingCompany = companyRepository.findByCompanyEmailId(emailId);
    if (existingCompany.isPresent()) {
        throw new AppException(
            ErrorMessageConstant.getFormattedMessage(
                ErrorMessageConstant.COMPANY_ALREADY_EXISTS_TEMPLATE,
                emailId
            ),
            HttpStatus.BAD_REQUEST
        );
    }
}
    @Override
    public List<CompanyDto> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(CompanyDto::entityToDto)
                .toList();
    }

    @Override
    public CompanyDto createCompany(CompanyDto companyDto) {

        validateCompanyEmailUnique(companyDto.getCompanyEmailId());
        
             if (!ValidationUtil.isValidEmail(companyDto.getCompanyEmailId())) {
                        throw new AppException(ErrorMessageConstant.INVALID_EMAIL_FORMAT, HttpStatus.BAD_REQUEST);
                    }
        Company company = new Company();
        company.setCompanyName(companyDto.getCompanyName());
        company.setCompanyEmailId(companyDto.getCompanyEmailId());
        company.setCompanyAddress(companyDto.getCompanyAddress());
        company.setMaxUsers(companyDto.getMaxUsers());

        Company savedCompany = companyRepository.save(company);

        return CompanyDto.entityToDto(savedCompany);
    }

    @Override
    public CompanyDto getCompanyById(Long companyId, User currentUser) {
        if (currentUser.getUserRole().getRoleName().equalsIgnoreCase(UserRoleConstants.ADMIN)) {
            Long adminCompanyId = currentUser.getCompany().getCompanyId();
            if (!adminCompanyId.equals(companyId)) {
                throw new AppException(ErrorMessageConstant.UNAUTHORIZED_TO_VIEW_COMPANY, HttpStatus.FORBIDDEN);
            }
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.COMPANY_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));

        return CompanyDto.entityToDto(company);
    }

    @Override
    public CompanyDto updateCompanybyId(Long companyId, CompanyDto companyDto, User currentUser) {

        if (currentUser.getUserRole().getRoleName().equalsIgnoreCase(UserRoleConstants.ADMIN)) {
            Long adminCompanyId = currentUser.getCompany().getCompanyId();
            if (!adminCompanyId.equals(companyId)) {
                throw new AppException(ErrorMessageConstant.UNAUTHORIZED_TO_UPDATE_COMPANY, HttpStatus.FORBIDDEN);
            }
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.COMPANY_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));

    // Only run email uniqueness check if the email is changing
    if (companyDto.getCompanyEmailId() != null &&
        !companyDto.getCompanyEmailId().equalsIgnoreCase(company.getCompanyEmailId())) {

         validateCompanyEmailUnique(companyDto.getCompanyEmailId());
    }

     if (!ValidationUtil.isValidEmail(companyDto.getCompanyEmailId())) {
                        throw new AppException(ErrorMessageConstant.INVALID_EMAIL_FORMAT, HttpStatus.BAD_REQUEST);
                    }

    if (companyDto.getCompanyName() != null && !companyDto.getCompanyName().isBlank()) {
        company.setCompanyName(companyDto.getCompanyName());
    }

        if (companyDto.getCompanyEmailId() != null && !companyDto.getCompanyEmailId().isBlank()) {
            company.setCompanyEmailId(companyDto.getCompanyEmailId());
        }

        if (companyDto.getCompanyAddress() != null && !companyDto.getCompanyAddress().isBlank()) {
            company.setCompanyAddress(companyDto.getCompanyAddress());
        }

        if (companyDto.getMaxUsers() != null &&
                currentUser.getUserRole().getRoleName().equalsIgnoreCase(UserRoleConstants.SUPER_ADMIN)) {
            company.setMaxUsers(companyDto.getMaxUsers());
        }

        Company updatedCompany = companyRepository.save(company);

        return CompanyDto.entityToDto(updatedCompany);
    }

    @Override
    public void deleteCompanybyId(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new AppException(ErrorMessageConstant.COMPANY_DOES_NOT_EXIST, HttpStatus.NOT_FOUND);

        }
        companyRepository.deleteById(companyId);
    }
}

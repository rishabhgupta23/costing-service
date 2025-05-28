package com.jubeiwato.costing_service.services.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.UserRoleConstants;
import com.jubeiwato.costing_service.dtos.CompanyDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.UserRepository;
import com.jubeiwato.costing_service.repositories.UserRoleRepository;
import com.jubeiwato.costing_service.services.CompanyService;
import com.jubeiwato.costing_service.services.UserService;
import com.jubeiwato.costing_service.entities.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public List<CompanyDto> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(CompanyDto::entityToDto)
                .toList();
    }

    @Override
    public CompanyDto createCompany(CompanyDto companyDto) {
        Company company = new Company();
        company.setCompanyName(companyDto.getCompanyName());
        company.setCompanyEmailId(companyDto.getCompanyEmailId());
        company.setCompanyAddress(companyDto.getCompanyAddress());
        company.setMaxUsers(6);

        Company savedCompany = companyRepository.save(company);

        return CompanyDto.entityToDto(savedCompany);
    }

    @Override
    public CompanyDto getCompanyById(Long companyId, User currentUser) {
        if (currentUser.getUserRole().getRoleName().equalsIgnoreCase(UserRoleConstants.ADMIN)) {
            Long adminCompanyId = currentUser.getCompany().getCompanyId();
            if (!adminCompanyId.equals(companyId)) {
                throw new AppException("You are not authorized to view details of this company", HttpStatus.FORBIDDEN);
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
                throw new AppException("You are not authorized to update this company", HttpStatus.FORBIDDEN);
            }
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.COMPANY_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));

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

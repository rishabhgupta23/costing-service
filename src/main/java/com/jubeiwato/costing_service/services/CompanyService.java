package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.dtos.CompanyDto;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.entities.Company;

public interface CompanyService {
    CompanyDto createCompany(CompanyDto companyDto);

    
    List<CompanyDto> getAllCompanies();

    CompanyDto getCompanyById(Long companyId);

    CompanyDto updateCompanybyId(Long companyId, CompanyDto companyDto);

    void deleteCompanybyId(Long companyId);
}

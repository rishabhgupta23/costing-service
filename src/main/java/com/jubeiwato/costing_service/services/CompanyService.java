package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.dtos.CompanyDto;

public interface CompanyService {
    CompanyDto createCompany(CompanyDto companyDto);

    List<CompanyDto> getAllCompanies();

    CompanyDto getCompanyById(Long companyId, User currentUser);

    CompanyDto updateCompanybyId(Long companyId, CompanyDto companyDto, User currentUser);

    void deleteCompanybyId(Long companyId);
}

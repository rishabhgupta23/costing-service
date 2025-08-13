package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.Company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {
    private Long companyId;
    private String companyName;
    private String companyAddress;
    private String companyEmailId;
    private Integer maxUsers;

    public static CompanyDto entityToDto(Company company) {
        return CompanyDto.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyAddress(company.getCompanyAddress())
                .companyEmailId(company.getCompanyEmailId())
                .maxUsers(company.getMaxUsers())
                .build();
    }
}

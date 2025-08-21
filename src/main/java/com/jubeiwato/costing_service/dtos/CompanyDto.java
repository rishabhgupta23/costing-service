package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.Company;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Company name cannot be null or blank.")
    private String companyName;
    
    @NotBlank(message = "Company address cannot be null or blank.")
    private String companyAddress;

    @NotBlank(message = "Company email ID cannot be null or blank.")
    private String companyEmailId;
    
    @Min(value = 1, message = "Max users must be greater than zero.")
    @NotNull(message = "Max users cannot be null or blank.")
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

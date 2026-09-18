package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyId(Long companyId);
    Optional<Company> findByCompanyEmailId(String companyEmailId);
}
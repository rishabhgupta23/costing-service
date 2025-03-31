package com.jubeiwato.costing_service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.CostFactor;

public interface CostFactorRepository extends JpaRepository<CostFactor, Long> {
    Page<CostFactor> findByCompany_CompanyId(Long companyId, Pageable pageable);
}

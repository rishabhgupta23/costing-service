package com.jubeiwato.costing_service.repositories;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.CostFactor;

public interface CostFactorRepository extends JpaRepository<CostFactor, Long> {
    Page<CostFactor> findByCompany_CompanyId(Long companyId, Pageable pageable);
    List<CostFactor> findByFactorIdInAndCompany_CompanyId(List<Long> factorIds, Long companyId);
}

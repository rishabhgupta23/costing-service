package com.jubeiwato.costing_service.repositories;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.CostFactor;

public interface CostFactorRepository extends JpaRepository<CostFactor, Long> {
    List<CostFactor> findByFactorIdInAndCompany_CompanyId(Set<Long> factorIds, Long companyId);
    Optional<CostFactor> findByFactorIdAndCompany_CompanyId(Long factorId, Long companyId);
    
    Optional<CostFactor> findByFactorIdAndCompany_CompanyIdAndDeleteFlag(Long factorId, Long companyId, Integer deleteFlag);
    Page<CostFactor> findByCompany_CompanyIdAndDeleteFlag(Long companyId, Integer deleteFlag, Pageable pageable);
    boolean existsByCompany_CompanyIdAndFactorNameAndDeleteFlag(Long companyId, String factorName, int deleteFlag);

}

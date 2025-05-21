package com.jubeiwato.costing_service.repositories;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jubeiwato.costing_service.entities.CostFactor;

public interface CostFactorRepository extends JpaRepository<CostFactor, Long>, JpaSpecificationExecutor<CostFactor> {
    List<CostFactor> findByFactorIdInAndCompany_CompanyId(Set<Long> factorIds, Long companyId);
    Optional<CostFactor> findByFactorIdAndCompany_CompanyIdAndDeleteFlag(Long factorId, Long companyId, Integer deleteFlag);
    Optional<CostFactor> findByCompany_CompanyIdAndFactorNameIgnoreCase(Long companyId, String name);

}

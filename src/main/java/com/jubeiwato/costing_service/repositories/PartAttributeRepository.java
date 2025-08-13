package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.PartAttribute;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartAttributeRepository extends JpaRepository<PartAttribute, Long> {

        Optional<PartAttribute> findByAttributeNameAndCompany_CompanyIdAndDeleteFlag(String attributeName,
                        Long companyId,
                        Integer deleteFlag);

    Optional<PartAttribute> findByAttributeIdAndCompany_CompanyId(Long attributeId, Long companyId);
    Page<PartAttribute> findAll(Specification<PartAttribute> spec, Pageable pageable);


    List<PartAttribute> findByAttributeIdInAndCompany(List<Long> distinctIds, Company companyId);
        Optional<PartAttribute> findByAttributeNameAndCompany_CompanyId(String attributeName, Long companyId);
        Optional<PartAttribute> findByAttributeIdAndDeleteFlag(Long attributeId, Integer deleteFlag);


}

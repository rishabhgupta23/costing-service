package com.jubeiwato.costing_service.repositories;

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
    List<PartAttribute> findAllByCompany_CompanyId(Long companyId);

    Optional<PartAttribute> findByAttributeId(Long attributeId);

    Page<PartAttribute> findAll(Specification<PartAttribute> spec, Pageable pageable);

    boolean existsByNameAndCompany_CompanyId(String name, Long companyId);
}

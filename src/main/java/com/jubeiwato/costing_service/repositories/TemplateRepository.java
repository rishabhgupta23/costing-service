package com.jubeiwato.costing_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.Template;

public interface TemplateRepository extends JpaRepository<Template, Long>{
     List<Template> findByCompany(Company company);
     boolean existsBytemplateNameIgnoreCaseAndCompany(String templateName, Company compayId);
         Page<Template> findAll(Specification<Template> spec, Pageable pageable);
    Optional<Template> findAllByTemplateIdAndCompany_CompanyId(Long templateId, Long companyId);
}

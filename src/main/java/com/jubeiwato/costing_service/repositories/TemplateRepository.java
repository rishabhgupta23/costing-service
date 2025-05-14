package com.jubeiwato.costing_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.Template;

public interface TemplateRepository extends JpaRepository<Template, Long>{
     List<Template> findByCompany(Company company);
Optional<Template> findByTemplateIdAndCompany(Long templateId, Company company);
     boolean existsByNameIgnoreCaseAndCompany(String name, Company company);
}

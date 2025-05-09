package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.Template;

public interface TemplateRepository extends JpaRepository<Template, Long>{
     boolean existsByNameIgnoreCaseAndCompany(String name, Company company);
}

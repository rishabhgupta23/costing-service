package com.jubeiwato.costing_service.repositories;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryIdAndCompany_CompanyId(Long categoryId, Long companyId);
}

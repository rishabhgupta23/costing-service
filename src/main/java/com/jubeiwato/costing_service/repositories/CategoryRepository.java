package com.jubeiwato.costing_service.repositories;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jubeiwato.costing_service.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsByCompany_CompanyIdAndCategoryName(Long companyId, String categoryName);
    Optional<Category> findByCategoryIdAndCompany_CompanyId(Long categoryId, Long companyId);
}

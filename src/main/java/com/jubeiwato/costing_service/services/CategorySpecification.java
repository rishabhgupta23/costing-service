package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.entities.Category;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CategorySpecification implements Specification<Category> {

    private final Long companyId;
    private final String categoryName;

    public CategorySpecification(Long companyId, String categoryName) {
        this.companyId = companyId;
        this.categoryName = categoryName;
    }

    @Override
    public Predicate toPredicate(Root<Category> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (companyId != null) {
        predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
        }
        
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("categoryName")), "%" + categoryName.toLowerCase() + "%"));
        }

        query.distinct(true);
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}

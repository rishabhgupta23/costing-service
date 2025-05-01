package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.entities.Category;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CategorySpecification implements Specification<Category> {

    private final Long companyId;
    private final String name;

    public CategorySpecification(Long companyId, String name) {
        this.companyId = companyId;
        this.name = name;
    }

    @Override
    public Predicate toPredicate(Root<Category> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (companyId != null) {
        predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
        }
        
        if (name != null && !name.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        query.distinct(true);
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}

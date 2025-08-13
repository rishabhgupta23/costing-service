package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.entities.PartAttribute;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PartAttributeSpecification {

    public static Specification<PartAttribute> getFilteredPartAttributes(Long companyId, String attributeName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("companyId"), companyId));
            }

            if (attributeName != null && !attributeName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("attributeName")),
                        "%" + attributeName.toLowerCase() + "%"));
            }

            predicates.add(criteriaBuilder.equal(root.get("deleteFlag"), 0));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.jubeiwato.costing_service.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.jubeiwato.costing_service.entities.Template;

import jakarta.persistence.criteria.Predicate;

public class TemplateSpecification {
     public static Specification<Template> getfilteredTemplates(Long companyId, String templateName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("companyId"), companyId));
            }

            if (templateName != null && !templateName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("templateName")),
                        "%" + templateName.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

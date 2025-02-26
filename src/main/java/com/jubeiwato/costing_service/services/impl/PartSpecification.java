package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.entities.Part;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class PartSpecification implements Specification<Part> {
    private final transient Part filter;

    public PartSpecification(Part filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<Part> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (filter == null) {
            return cb.conjunction();
        }
        List<Predicate> predicates = new ArrayList<>();

        validateInput(filter);

        if (filter.getPartName() != null && !filter.getPartName().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("partName")), "%" + filter.getPartName().toLowerCase() + "%"));
        }
        if (filter.getPartNumber() != null && !filter.getPartNumber().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("partNumber")), "%" + filter.getPartNumber().toLowerCase() + "%"));
        }
        if (filter.getCategoryName() != null && !filter.getCategoryName().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("categoryName")), filter.getCategoryName().toLowerCase()));
        }
        if (filter.getType() != null) {
            predicates.add(cb.equal(root.get("type"), filter.getType()));
        }
        if (filter.getUnit() != null && !filter.getUnit().isEmpty()) {
            predicates.add(cb.equal(cb.lower(root.get("unit")), filter.getUnit().toLowerCase()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private void validateInput(Part filter) {
        if (filter == null) return;

        validateField(filter.getPartName(), "partName", "^[a-zA-Z0-9 _-]+$");
        validateField(filter.getPartNumber(), "partNumber", "^[a-zA-Z0-9_-]+$");
        validateField(filter.getCategoryName(), "categoryName", "^[a-zA-Z0-9 _-]+$");
        validateField(filter.getUnit(), "unit", "^[a-zA-Z]+$");

        if (filter.getType() != null && !EnumSet.allOf(PartType.class).contains(filter.getType())) {
            throw new IllegalArgumentException("Invalid part type");
        }
    }

    private void validateField(String value, String fieldName, String regex) {
        if (value != null && !value.matches(regex)) {
            throw new IllegalArgumentException("Invalid " + fieldName);
        }
    }

}

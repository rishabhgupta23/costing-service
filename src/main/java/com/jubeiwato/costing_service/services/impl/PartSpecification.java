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

        if (!isValidInput(filter.getPartName()) ||
                !isValidInput(filter.getPartNumber()) ||
                !isValidInput(filter.getCategoryName()) ||
                !isValidInput(filter.getUnit())) {
            throw new IllegalArgumentException("Invalid input: Prohibited characters detected.");
        }

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

    private static boolean isValidInput(String input) {
        if (input == null || input.trim().isEmpty()) return true;
        String regex = ".*[';#% _*/=\\-].*";
        return !input.matches(regex);
    }

}

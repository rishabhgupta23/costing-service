package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.entities.Part;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class PartSpecification implements Specification<Part> {
    private final Part filter;

    public PartSpecification(Part filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<Part> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (filter == null) {
            return cb.conjunction();
        }
        List<Predicate> predicates = new ArrayList<>();

//        if (filter.getPartId() != null) {
//            predicates.add(cb.equal(root.get("partId"), filter.getPartId()));
//        }
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
}

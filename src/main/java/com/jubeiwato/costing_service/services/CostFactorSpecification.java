package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.entities.CostFactor;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CostFactorSpecification implements Specification<CostFactor> {

    private final Long companyId;
    private final String factorName;

    public CostFactorSpecification(Long companyId, String factorName) {
        this.companyId = companyId;
        this.factorName = factorName;
    }

    @Override
    public Predicate toPredicate(Root<CostFactor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (companyId != null) {
            predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
        }

        if (root.get("deleteFlag") != null) {
            predicates.add(cb.equal(root.get("deleteFlag"), 0));
        }

        if (factorName != null && !factorName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("factorName")), "%" + factorName.toLowerCase() + "%"));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}

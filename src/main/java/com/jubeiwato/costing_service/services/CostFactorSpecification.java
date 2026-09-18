package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.entities.CostFactor;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CostFactorSpecification implements Specification<CostFactor> {

    private final Long companyId;
    private final String factorName;
    private final Integer deleteFlag;

    public CostFactorSpecification(Long companyId, String factorName, Integer deleteFlag) {
        this.companyId = companyId;
        this.factorName = factorName;
        this.deleteFlag = deleteFlag;
    }

    @Override
    public Predicate toPredicate(Root<CostFactor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (companyId != null) {
            predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
        }

        if (root.get("deleteFlag") != null) {
            predicates.add(cb.equal(root.get("deleteFlag"), deleteFlag));
        }

        if (factorName != null && !factorName.trim().isEmpty()) {
            String search = factorName.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("factorName")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(cb.lower(root.get("factorName")), search + "%"),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(root.get("factorName"))
            );
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}

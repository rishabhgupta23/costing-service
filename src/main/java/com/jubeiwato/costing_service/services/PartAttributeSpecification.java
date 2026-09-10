package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.entities.PartAttribute;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PartAttributeSpecification implements Specification<PartAttribute> {

    private final Long companyId;
    private final String attributeName;
    private final Integer deleteFlag;

    public PartAttributeSpecification(Long companyId, String attributeName, Integer deleteFlag) {
        this.companyId = companyId;
        this.attributeName = attributeName;
        this.deleteFlag = deleteFlag;
    }

    @Override
    public Predicate toPredicate(Root<PartAttribute> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (companyId != null) {
            predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
        }

        if (attributeName != null && !attributeName.trim().isEmpty()) {
            String search = attributeName.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("attributeName")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(cb.lower(root.get("attributeName")), search + "%"),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(root.get("attributeName"))
            );
        }

        if (root.get("deleteFlag") != null) {
            predicates.add(cb.equal(root.get("deleteFlag"), deleteFlag));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}

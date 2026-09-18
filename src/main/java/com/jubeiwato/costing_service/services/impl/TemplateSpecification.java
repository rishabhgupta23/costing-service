package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.entities.Template;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TemplateSpecification implements Specification<Template> {

    private final Long companyId;
    private final String templateName;

    public TemplateSpecification(
            Long companyId,
            String templateName) {

        this.companyId = companyId;
        this.templateName = templateName;
    }

    @Override
    public Predicate toPredicate(
            Root<Template> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        // Company filter
        if (companyId != null) {

            predicates.add(
                    cb.equal(
                            root.get("company").get("companyId"),
                            companyId
                    )
            );
        }

        // Template Name
        if (templateName != null && !templateName.trim().isEmpty()) {

            String search =
                    templateName.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("templateName")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(
                                                            root.get("templateName")
                                                    ),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(
                            root.get("templateName")
                    )
            );
        }

        query.distinct(true);

        return cb.and(
                predicates.toArray(new Predicate[0])
        );
    }
}
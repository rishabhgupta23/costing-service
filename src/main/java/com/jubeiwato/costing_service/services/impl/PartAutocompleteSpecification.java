package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.entities.Part;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class PartAutocompleteSpecification implements Specification<Part> {

    private final Long companyId;
    private final String search;

    public PartAutocompleteSpecification(Long companyId, String search) {
        this.companyId = companyId;
        this.search = search;
    }

    @Override
    public Predicate toPredicate(
            Root<Part> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        String searchValue = search.trim().toLowerCase();

        // Company filter
        Predicate companyPredicate = cb.equal(
                root.get("company").get("companyId"),
                companyId
        );

        // Search by part name
        Predicate partNamePredicate = cb.like(
                cb.lower(root.get("partName")),
                "%" + searchValue + "%"
        );

        // Search by part number
        Predicate partNumberPredicate = cb.like(
                cb.lower(root.get("partNumber")),
                "%" + searchValue + "%"
        );

        // Match either part name OR part number
        Predicate searchPredicate = cb.or(
                partNamePredicate,
                partNumberPredicate
        );

        /*
         * Ordering:
         *
         * 1. Part name starting with search text
         * 2. Part number starting with search text
         * 3. Part name alphabetically
         */
        query.orderBy(
                cb.asc(
                        cb.selectCase()
                                .when(
                                        cb.like(
                                                cb.lower(root.get("partName")),
                                                searchValue + "%"
                                        ),
                                        0
                                )
                                .otherwise(1)
                ),
                cb.asc(
                        cb.selectCase()
                                .when(
                                        cb.like(
                                                cb.lower(root.get("partNumber")),
                                                searchValue + "%"
                                        ),
                                        0
                                )
                                .otherwise(1)
                ),
                cb.asc(root.get("partName"))
        );

        return cb.and(
                companyPredicate,
                searchPredicate
        );
    }
}
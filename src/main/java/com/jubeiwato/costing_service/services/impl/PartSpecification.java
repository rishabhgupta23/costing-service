package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.entities.Part;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PartSpecification implements Specification<Part> {

    private final Long companyId;
    private final String partName;
    private final String partNumber;
    private final String categoryName;
    private final String type;
    private final String unit;

    public PartSpecification(
            long companyId,
            String partName,
            String partNumber,
            String categoryName,
            String type,
            String unit) {

        this.companyId = companyId;
        this.partName = partName;
        this.partNumber = partNumber;
        this.categoryName = categoryName;
        this.type = type;
        this.unit = unit;
    }

    @Override
    public Predicate toPredicate(
            Root<Part> root,
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


        // ---------------------------------------------------------
        // Part Name
        // ---------------------------------------------------------
        if (partName != null && !partName.trim().isEmpty()) {

            String search =
                    partName.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("partName")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(
                                                            root.get("partName")
                                                    ),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(
                            root.get("partName")
                    )
            );
        }


        // ---------------------------------------------------------
        // Part Number
        // ---------------------------------------------------------
        if (partNumber != null && !partNumber.trim().isEmpty()) {

            String search =
                    partNumber.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("partNumber")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(
                                                            root.get("partNumber")
                                                    ),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(
                            root.get("partNumber")
                    )
            );
        }


        // ---------------------------------------------------------
        // Category Name
        // ---------------------------------------------------------
        if (categoryName != null
                && !categoryName.trim().isEmpty()) {

            String search =
                    categoryName.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(
                                    root.get("category")
                                            .get("categoryName")
                            ),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(
                                                            root.get("category")
                                                                    .get("categoryName")
                                                    ),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(
                            root.get("category")
                                    .get("categoryName")
                    )
            );
        }


        // ---------------------------------------------------------
        // Type
        // ---------------------------------------------------------
        if (type != null && !type.trim().isEmpty()) {

            String search =
                    type.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("type")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(
                                                            root.get("type")
                                                    ),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(
                            root.get("type")
                    )
            );
        }


        // ---------------------------------------------------------
        // Unit
        // ---------------------------------------------------------
        if (unit != null && !unit.trim().isEmpty()) {

            String search =
                    unit.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("unit")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(
                                                            root.get("unit")
                                                    ),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(
                            root.get("unit")
                    )
            );
        }


        query.distinct(true);

        return cb.and(
                predicates.toArray(new Predicate[0])
        );
    }
}
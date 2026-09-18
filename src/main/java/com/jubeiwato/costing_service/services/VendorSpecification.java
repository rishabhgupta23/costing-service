package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.entities.Vendor;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VendorSpecification implements Specification<Vendor> {

    private final Long companyId;
    private final String vendorName;
    private final String address;
    private final String emailId;
    private final String contactNumber;

    public VendorSpecification(
            Long companyId,
            String vendorName,
            String address,
            String emailId,
            String contactNumber) {

        this.companyId = companyId;
        this.vendorName = vendorName;
        this.address = address;
        this.emailId = emailId;
        this.contactNumber = contactNumber;
    }

    @Override
    public Predicate toPredicate(
            Root<Vendor> root,
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

        // Vendor Name
        if (vendorName != null && !vendorName.trim().isEmpty()) {

            String search = vendorName.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("vendorName")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(root.get("vendorName")),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(root.get("vendorName"))
            );
        }

        // Address
        if (address != null && !address.trim().isEmpty()) {

            String search = address.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("address")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(root.get("address")),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(root.get("address"))
            );
        }

        // Email
        if (emailId != null && !emailId.trim().isEmpty()) {

            String search = emailId.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("emailId")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(root.get("emailId")),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(root.get("emailId"))
            );
        }

        // Contact Number
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {

            String search = contactNumber.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("contactNumber")),
                            "%" + search + "%"
                    )
            );

            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(root.get("contactNumber")),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(root.get("contactNumber"))
            );
        }

        query.distinct(true);

        return cb.and(
                predicates.toArray(new Predicate[0])
        );
    }
}
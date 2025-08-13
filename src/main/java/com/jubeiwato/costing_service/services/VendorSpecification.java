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


    public VendorSpecification(Long companyId,String vendorName, String address, String emailId, String contactNumber) {
        this.companyId=companyId;
        this.vendorName = vendorName;
        this.address = address;
        this.emailId = emailId;
        this.contactNumber = contactNumber;
    }

    @Override
    public Predicate toPredicate(Root<Vendor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (companyId != null) {
        predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
        }
        if (vendorName != null && !vendorName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("vendorName")), "%" + vendorName.toLowerCase() + "%"));
        }
        if (address != null && !address.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
        }
        if (emailId != null && !emailId.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("emailId")), "%" + emailId.toLowerCase() + "%"));
        }
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("contactNumber")), "%" + contactNumber.toLowerCase() + "%"));
        }

        query.distinct(true);
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}

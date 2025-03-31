package com.jubeiwato.costing_service.services;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.utils.ValidationUtil;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class VendorSpecification {
    public static Specification<Vendor> getFilteredVendors(Long companyId, String name, String address, String emailId, String contactNumber) {

        if (!ValidationUtil.isValidInput(name) || 
        !ValidationUtil.isValidInput(address) || 
        !ValidationUtil.isValidInput(emailId) || 
        !ValidationUtil.isValidInput(contactNumber)) {
        throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
    }
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("company").get("companyId"), companyId));
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (address != null && !address.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
            }
            if (emailId != null && !emailId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("emailId")), "%" + emailId.toLowerCase() + "%"));
            }
            if (contactNumber != null && !contactNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("contactNumber")), "%" + contactNumber.toLowerCase() + "%"));
            }
    

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}

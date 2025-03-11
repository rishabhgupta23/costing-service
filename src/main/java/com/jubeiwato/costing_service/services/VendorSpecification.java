package com.jubeiwato.costing_service.services;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import com.jubeiwato.costing_service.configue.AppException;
import com.jubeiwato.costing_service.entities.Vendor;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class VendorSpecification {
    public static Specification<Vendor> getFilteredVendors(String name, String address, String emailId, String contactNumber) {

        if (!isValidInput(name) || 
        !isValidInput(address) || 
        !isValidInput(emailId) || 
        !isValidInput(contactNumber)) {
        throw new AppException("Invalid input: Prohibited characters detected.", HttpStatus.BAD_REQUEST);
    }
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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

    private static boolean isValidInput(String input) {
        if (input == null || input.trim().isEmpty()) return true;
    
        String regex = ".*[';#% _*/=\\-].*"; 
    
        return !input.matches(regex);
    }

}

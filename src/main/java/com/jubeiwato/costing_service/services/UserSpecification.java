package com.jubeiwato.costing_service.services;

import org.springframework.data.jpa.domain.Specification;
import com.jubeiwato.costing_service.entities.User;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> getFilteredUsers(Long companyId, String fullName, String emailId, String roleName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Ensure company filter is always applied
            predicates.add(criteriaBuilder.equal(root.get("company").get("companyId"), companyId));

            // Apply filters only if values are provided
            if (fullName != null && !fullName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%"));
            }
            if (emailId != null && !emailId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("emailId")), "%" + emailId.toLowerCase() + "%"));
            }
            if (roleName != null && !roleName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("role").get("roleName")), "%" + roleName.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.jubeiwato.costing_service.services;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.utils.ValidationUtil;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> getFilteredUsers(Long companyId, String displayName, String emailId, String roleName) {
        if (!ValidationUtil.isValidInput(displayName) || 
        !ValidationUtil.isValidInput(emailId) || 
        !ValidationUtil.isValidInput(roleName)){
        throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
    }
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Ensure company filter is always applied
            predicates.add(criteriaBuilder.equal(root.get("company").get("companyId"), companyId));

            // Apply filters only if values are provided
            if (displayName != null && !displayName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("displayName")), "%" + displayName.toLowerCase() + "%"));
            }
            if (emailId != null && !emailId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("emailId")), "%" + emailId.toLowerCase() + "%"));
            }
            if (roleName != null && !roleName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("userRole").get("roleName")), "%" + roleName.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

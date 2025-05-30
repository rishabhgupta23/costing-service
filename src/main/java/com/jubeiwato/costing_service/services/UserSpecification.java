package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.entities.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification implements Specification<User> {

    private final Long companyId;
    private final String displayName;
    private final String emailId;
    private final String roleName;

    public UserSpecification(Long companyId, String displayName, String emailId, String roleName) {
        this.companyId = companyId;
        this.displayName = displayName;
        this.emailId = emailId;
        this.roleName = roleName;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("company").get("companyId"), companyId));

        if (displayName != null && !displayName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("displayName")), "%" + displayName.toLowerCase() + "%"));
        }
        if (emailId != null && !emailId.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("emailId")), "%" + emailId.toLowerCase() + "%"));
        }
        if (roleName != null && !roleName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("userRole").get("roleName")), "%" + roleName.toLowerCase() + "%"));
        }

        query.distinct(true);
        return cb.and(predicates.toArray(new Predicate[0]));
    }

public static Specification<User> orderByRoleName(Sorting sortMode) {
    return (root, query, builder) -> {
        Fetch<Object, Object> userRoleFetch = root.fetch("userRole", JoinType.LEFT);
        Join<Object, Object> userRoleJoin = (Join<Object, Object>) userRoleFetch;
        if (userRoleJoin != null) {
        query.orderBy(sortMode == Sorting.DESC
                ? builder.desc(userRoleJoin.get("roleName"))
                : builder.asc(userRoleJoin.get("roleName")));
        
        }
        return builder.conjunction();
    };
        
}

}

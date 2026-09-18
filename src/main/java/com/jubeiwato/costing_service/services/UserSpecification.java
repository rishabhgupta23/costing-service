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

    public UserSpecification(
            Long companyId,
            String displayName,
            String emailId,
            String roleName) {

        this.companyId = companyId;
        this.displayName = displayName;
        this.emailId = emailId;
        this.roleName = roleName;
    }

    @Override
    public Predicate toPredicate(
            Root<User> root,
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
        if (displayName != null && !displayName.trim().isEmpty()) {

            String search = displayName.trim().toLowerCase();

            // Keep all records containing the search text
            predicates.add(
                    cb.like(
                            cb.lower(root.get("displayName")),
                            "%" + search + "%"
                    )
            );

            // Starts-with results come first
            query.orderBy(
                    cb.asc(
                            cb.selectCase()
                                    .when(
                                            cb.like(
                                                    cb.lower(root.get("displayName")),
                                                    search + "%"
                                            ),
                                            0
                                    )
                                    .otherwise(1)
                    ),
                    cb.asc(root.get("displayName"))
            );
        }

        if (emailId != null && !emailId.trim().isEmpty()) {

            String search = emailId.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("emailId")),
                            "%" + search + "%"
                    )
            );

            // If email search is being used, starts-with emails come first
            if (displayName == null || displayName.trim().isEmpty()) {
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
        }

        if (roleName != null && !roleName.trim().isEmpty()) {

            String search = roleName.trim().toLowerCase();

            predicates.add(
                    cb.like(
                            cb.lower(root.get("userRole").get("roleName")),
                            "%" + search + "%"
                    )
            );

            // If role search is being used and displayName/email are not,
            // starts-with roles come first
            if ((displayName == null || displayName.trim().isEmpty())
                    && (emailId == null || emailId.trim().isEmpty())) {

                query.orderBy(
                        cb.asc(
                                cb.selectCase()
                                        .when(
                                                cb.like(
                                                        cb.lower(
                                                                root.get("userRole")
                                                                        .get("roleName")
                                                        ),
                                                        search + "%"
                                                ),
                                                0
                                        )
                                        .otherwise(1)
                        ),
                        cb.asc(
                                root.get("userRole").get("roleName")
                        )
                );
            }
        }

        query.distinct(true);

        return cb.and(
                predicates.toArray(new Predicate[0])
        );
    }

    public static Specification<User> orderByRoleName(Sorting sortMode) {

        return (root, query, builder) -> {

            Join<Object, Object> userRoleJoin =
                    root.join("userRole", JoinType.LEFT);

            query.orderBy(
                    sortMode == Sorting.DESC
                            ? builder.desc(userRoleJoin.get("roleName"))
                            : builder.asc(userRoleJoin.get("roleName"))
            );

            return builder.conjunction();
        };
    }
}

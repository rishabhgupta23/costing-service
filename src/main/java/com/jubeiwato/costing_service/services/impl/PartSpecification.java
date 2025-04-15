package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.utils.ValidationUtil;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class PartSpecification implements Specification<Part> {
    private final String partName;
    private final String partNumber;
    private final String categoryName;
    private final String type;
    private final String unit;

    public PartSpecification(String partName, String partNumber, String categoryName, String type, String unit) {
        this.partName = partName;
        this.partNumber = partNumber;
        this.categoryName = categoryName;
        this.type = type;
        this.unit = unit;
    }

    @Override
    public Predicate toPredicate(Root<Part> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();


        if (!ValidationUtil.isValidInput(partName) ||
                !ValidationUtil.isValidInput(partNumber) ||
                !ValidationUtil.isValidInput(categoryName) ||
                !ValidationUtil.isValidInput(unit)) {
            throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        if (partName != null && !partName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("partName")), "%" + partName.toLowerCase() + "%"));
        }
        if (partNumber != null && !partNumber.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("partNumber")), "%" + partNumber.toLowerCase() + "%"));
        }
        if (categoryName != null && !categoryName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("categoryName")), "%" + categoryName.toLowerCase() + "%"));
        }
        if (type != null && !type.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("type")), "%" + type.toLowerCase() + "%"));
        }
        if (unit != null && !unit.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("unit")), "%" + unit.toLowerCase() + "%"));
        }

        query.distinct(true);
        return cb.and(predicates.toArray((new Predicate[predicates.size()])));
    }

}

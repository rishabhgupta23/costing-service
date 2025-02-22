package com.jubeiwato.costing_service.repositories;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jubeiwato.costing_service.entities.PartUnit;

public interface PartUnitRepository extends JpaRepository<PartUnit, Long> {
    Optional<PartUnit> findByUnitName(String unitName);
}

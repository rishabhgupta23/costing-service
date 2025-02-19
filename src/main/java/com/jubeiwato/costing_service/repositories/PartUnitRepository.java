package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jubeiwato.costing_service.entities.PartUnit;

public interface PartUnitRepository extends JpaRepository<PartUnit, Long> {
   
}

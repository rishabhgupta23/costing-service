package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Part;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jubeiwato.costing_service.entities.PartCost;

public interface PartCostRepository extends JpaRepository<PartCost, Long> {
    List<PartCost> findByPart(Part part);
}

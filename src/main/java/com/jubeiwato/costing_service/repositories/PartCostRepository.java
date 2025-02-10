package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Part;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.PartCost;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartCostRepository extends JpaRepository<PartCost, Long> {
    List<PartCost> findByPart(Part part);

}

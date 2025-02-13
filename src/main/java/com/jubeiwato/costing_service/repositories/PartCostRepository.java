package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.PartCost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartCostRepository extends JpaRepository<PartCost, Long> {
    List<PartCost> findByPart(Part part);

    @Query("SELECT p1 FROM PartCost p1 " +
            "INNER JOIN PartCost p2 " +
            "ON p1.part.id = p2.part.id AND p1.vendor.id = p2.vendor.id " +
            "WHERE p1.updatedBy > p2.updatedBy AND p1.part.id = :partId")
    List<PartCost> findLatestByPart(@Param("partId") Long partId);

}

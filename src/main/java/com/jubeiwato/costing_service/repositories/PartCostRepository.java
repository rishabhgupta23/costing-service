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
            "WHERE p1.updatedDateTime = (" +
            "    SELECT MAX(p2.updatedDateTime) " +
            "    FROM PartCost p2 " +
            "    WHERE p2.part.id = :partId AND p2.vendor.id = p1.vendor.id" +
            ") AND p1.part.id = :partId")

    List<PartCost> findByPartId(@Param("partId") Long partId);


    @Query(value = "SELECT MAX(vendor_count) FROM " +"(SELECT COUNT(DISTINCT vendor_id) AS vendor_count " +
   " FROM app.part_cost GROUP BY part_id) AS subquery",
    nativeQuery = true)
Integer getMaxVendorCount();
}

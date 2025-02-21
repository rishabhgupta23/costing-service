package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.jubeiwato.costing_service.entities.PartCost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query(value = "SELECT p.part_id, p.part_name, p.part_number, p.category_name, p.type, p.unit, " +
    "STRING_AGG(DISTINCT v.name, ', ') AS vendors " +
    "FROM app.part p " +
    "LEFT JOIN app.part_cost pc ON p.part_id = pc.part_id " +
    "LEFT JOIN app.vendor v ON pc.vendor_id = v.vendor_id " +
    "GROUP BY p.part_id " +
    "ORDER BY p.part_id " +
    "OFFSET :#{#pageable.offset} LIMIT :#{#pageable.pageSize}",
    countQuery = "SELECT COUNT(p.part_id) FROM app.part p",
    nativeQuery = true)
Page<Object[]> getPartVendorList(Pageable pageable);

   @Query(value = "SELECT MAX(vendor_count) FROM " +"(SELECT COUNT(DISTINCT vendor_id) AS vendor_count " +
   " FROM app.part_cost GROUP BY part_id) AS subquery",
    nativeQuery = true)
Integer getMaxVendorCount();
}

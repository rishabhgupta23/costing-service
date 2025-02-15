package com.jubeiwato.costing_service.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.jubeiwato.costing_service.entities.PartCost;

public interface PartCostRepository extends JpaRepository<PartCost, Long> {
    @Query(value = "SELECT p.part_id AS part, STRING_AGG(DISTINCT v.name, ',') AS vendors " +
    "FROM app.part_cost pc " +
    "JOIN app.part p ON p.part_id = pc.part_id " +
    "JOIN app.vendor v ON v.vendor_id = pc.vendor_id " +
    "GROUP BY p.part_id",
nativeQuery = true)
List<Object[]> getPartVendorList();

}

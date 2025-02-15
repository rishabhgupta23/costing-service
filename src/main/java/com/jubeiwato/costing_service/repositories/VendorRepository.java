package com.jubeiwato.costing_service.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.jubeiwato.costing_service.entities.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
  @Query(value = "SELECT MAX(vendor_count) FROM " +
               "(SELECT COUNT(DISTINCT vendor_id) AS vendor_count " +
               " FROM app.part_cost GROUP BY part_id) AS subquery", 
       nativeQuery = true)
Integer getMaxVendorCount();
}




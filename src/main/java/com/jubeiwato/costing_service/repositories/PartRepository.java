package com.jubeiwato.costing_service.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.Vendor;

public interface PartRepository extends JpaRepository<Part, Long>{
   @Query("SELECT p FROM Part p JOIN PartCost pc ON p.partId = pc.part.partId WHERE pc.vendor.vendorId = :vendorId")
   List<Part> getVendorParts(@Param("vendorId") Long vendorId);

   @Query("SELECT v FROM Vendor v WHERE v.vendorId IN " +
       "(SELECT MIN(pc.vendor.vendorId) FROM PartCost pc WHERE pc.part = :part GROUP BY pc.vendor.vendorId)")
   List<Vendor> findDistinctVendorsByPart(@Param("part") Part part);

}

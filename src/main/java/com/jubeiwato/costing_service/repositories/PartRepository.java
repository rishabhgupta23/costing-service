package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.jubeiwato.costing_service.entities.Part;

public interface PartRepository extends JpaRepository<Part, Long>{
   @Query("SELECT p FROM Part p JOIN PartCost pc ON p.partId = pc.part.partId WHERE pc.vendor.vendorId = :vendorId")
   Page<Part> getVendorParts(@Param("vendorId") Long vendorId,Pageable pageable);


}

package com.jubeiwato.costing_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jubeiwato.costing_service.dtos.GetVendorPartDto;
import com.jubeiwato.costing_service.entities.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    @Query("SELECT new com.jubeiwato.costing_service.dtos.GetVendorPartDto(p.partName, p.partNumber) " +
       "FROM Part p " +
       "JOIN PartCost pc ON p.partId = pc.part.partId " +
       "JOIN Vendor v ON pc.vendor.vendorId = v.vendorId " +
       "WHERE v.vendorId = :vendorId")
List<GetVendorPartDto> getVendorParts(@Param("vendorId") Long vendorId);

}




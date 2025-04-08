package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.jubeiwato.costing_service.entities.Part;

public interface PartRepository extends JpaRepository<Part, Long>, JpaSpecificationExecutor<Part> {
   @Query("SELECT p FROM Part p JOIN PartCost pc ON p.partId = pc.part.partId WHERE pc.vendor.vendorId = :vendorId")
   Page<Part> getVendorParts(@Param("vendorId") Long vendorId,Pageable pageable);

   List<Part> findByCompany_CompanyId(Long companyId, Sort sort);

   boolean existsByCompany_CompanyIdAndPartNumber(Long companyId, String partNumber);
   List<Part> findByPartIdInAndCompany_CompanyId(Set<Long> partIds, Long companyId);
}

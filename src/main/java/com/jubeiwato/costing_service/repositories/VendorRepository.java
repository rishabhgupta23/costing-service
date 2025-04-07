package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.jubeiwato.costing_service.entities.Vendor;
import java.util.List;

public interface VendorRepository extends JpaRepository<Vendor, Long>, JpaSpecificationExecutor<Vendor> {
    List<Vendor> findByCompanyCompanyId(Long companyId);
    List<Vendor> findByVendorIdInAndCompany_CompanyId(List<Long> vendorIds, Long companyId);

}




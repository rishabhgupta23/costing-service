package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jubeiwato.costing_service.entities.Vendor;
import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long>, JpaSpecificationExecutor<Vendor> {
    List<Vendor> findByCompanyCompanyId(Long companyId);

    Optional<Vendor> findByVendorIdAndCompany_CompanyId(Long vendorId, Long companyId);

    boolean existsByCompanyCompanyIdAndName(Long companyId, String name);

    boolean existsByCompanyCompanyIdAndNameAndVendorIdNot(Long companyId, String name, Long vendorId);

}




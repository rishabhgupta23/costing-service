package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jubeiwato.costing_service.entities.Vendor;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VendorRepository extends JpaRepository<Vendor, Long>, JpaSpecificationExecutor<Vendor> {
    List<Vendor> findByCompanyCompanyId(Long companyId);

    Optional<Vendor> findByVendorIdAndCompany_CompanyId(Long vendorId, Long companyId);

    boolean existsByCompanyCompanyIdAndVendorName(Long companyId, String vendorName);
    
    boolean existsByCompanyCompanyIdAndVendorNameAndVendorIdNot(Long companyId, String vendorName, Long vendorId);

    List<Vendor> findByVendorIdInAndCompany_CompanyId(Set<Long> vendorIds, Long companyId);

    boolean existsByCompanyCompanyIdAndVendorNameIgnoreCase(Long companyId, String vendorName);

    boolean existsByCompanyCompanyIdAndVendorIdNotAndVendorNameIgnoreCase(Long companyId, Long vendorId, String vendorName);

}
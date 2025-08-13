package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.VendorDto;
import java.io.IOException;

public interface VendorService {
        void createVendor(Long companyId, String vendorName, String emailId, String contactNumber, String address);

        ApiPageResponseDto<List<VendorDto>> getVendorList(Long companyId, String vendorName, String address, String emailId,
                        String contactNumber, int pageNo, int pageSize, String sortColumn, Sorting sortMode);

        VendorDto getVendorById(Long id, Long companyId);

        VendorDto updateVendorById(Long id, String vendorName, String emailId, String contactNumber, String address,
                        Long companyId);

        void deleteVendorById(Long id, Long companyId);

        ApiPageResponseDto<List<PartDto>> getVendorParts(Long vendorId, int pageNo, int pageSize, Long companyId);

        byte[] downloadVendorExcel(Long companyId) throws IOException;

}

package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.VendorDto;

public interface VendorService {
    void createVendor(String name, String emailId, String contactNumber, String address);
    
    ApiPageResponseDto<List<VendorDto>> getVendorList(int pageNo, int size);

    VendorDto getVendorById(Long id);

    VendorDto updateVendorById(Long id, String name, String emailId, String contactNumber, String address);

    void deleteVendorById(Long id);

    ApiPageResponseDto<List<PartDto>> getVendorParts(Long vendorId,int page, int size);

}

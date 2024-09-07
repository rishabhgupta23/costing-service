package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.dtos.VendorDto;

public interface VendorService {
    void createVendor(String name, String emailId, String contactNumber, String address);
    
    List<VendorDto> getVendorList();

    VendorDto getVendorById(Long id);

    VendorDto updateVendorById(Long id, String name, String emailId, String contactNumber, String address);

    void deleteVendorById(Long id);
}

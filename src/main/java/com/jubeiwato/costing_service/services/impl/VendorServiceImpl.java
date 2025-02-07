package com.jubeiwato.costing_service.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.dtos.GetVendorPartDto;
import com.jubeiwato.costing_service.dtos.VendorDto;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.exceptions.NotFoundException;
import com.jubeiwato.costing_service.repositories.VendorRepository;
import com.jubeiwato.costing_service.services.VendorService;

@Service
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;

    public VendorServiceImpl(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    public void createVendor(String name, String emailId, String contactNumber, String address) {
        Vendor vendor = Vendor.builder()
        .name(name)
        .emailId(emailId)
        .contactNumber(contactNumber)
        .address(address)
        .build();

        vendorRepository.save(vendor);
    }

    @Override
    public List<VendorDto> getVendorList() {
        return this.vendorRepository.findAll().stream().map(VendorDto::entityToDto).toList();
    }

    @Override
    public VendorDto getVendorById(Long id) {
        Vendor vendor = this.vendorRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Vendor does not exist"));

        return VendorDto.entityToDto(vendor);
    }

    @Override
    public VendorDto updateVendorById(Long id, String name, String emailId, String contactNumber, String address) {
        Vendor vendor = this.vendorRepository.getReferenceById(id);
        vendor.setName(name);
        vendor.setEmailId(emailId);
        vendor.setContactNumber(contactNumber);
        vendor.setAddress(address);

        return VendorDto.entityToDto(this.vendorRepository.save(vendor));
    }

    @Override
    public void deleteVendorById(Long id) {
        Vendor vendor = this.vendorRepository.getReferenceById(id);
        vendor.setDeleteFlag(DeleteFlag.POSTITVE.getValue());
        this.vendorRepository.save(vendor);
    }
    public List<GetVendorPartDto> getVendorParts(Long vendorId) {
    return vendorRepository.getVendorParts(vendorId);
}


}

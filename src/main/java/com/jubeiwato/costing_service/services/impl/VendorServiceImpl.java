package com.jubeiwato.costing_service.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.VendorDto;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.exceptions.NotFoundException;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.repositories.VendorRepository;
import com.jubeiwato.costing_service.services.VendorService;

@Service
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final PartRepository partRepository;
    
    public VendorServiceImpl(VendorRepository vendorRepository,PartRepository partRepository) {
        this.vendorRepository = vendorRepository;
        this.partRepository= partRepository;
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
    public ApiPageResponseDto<List<VendorDto>> getVendorList(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize); 
        Page<Vendor> vendorPage = this.vendorRepository.findAll(pageable); 

        List<VendorDto> vendorDtos = vendorPage.getContent().stream()
                .map(VendorDto::entityToDto)
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
             .totalPages(vendorPage.getTotalPages())
             .pageNumber(pageNo)
             .pageSize(pageSize)
             .totalRecords(vendorPage.getTotalElements())
                .build();

        return ApiPageResponseDto.<List<VendorDto>>builder()
                .data(vendorDtos)
                .pageInfo(pageInfo)
                .build();
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
   
    @Transactional
    @Override
    public void deleteVendorById(Long id) {
      
        vendorRepository.deleteById(id);
    }
    
    @Override
    public ApiPageResponseDto<List<PartDto>> getVendorParts(Long vendorId, int pageNo, int pageSize) {
    PageRequest pageable = PageRequest.of(pageNo, pageSize);
    Page<Part> partVendorList = partRepository.getVendorParts(vendorId, pageable);

    List<PartDto> partDtos = partVendorList.getContent().stream()
        .map(PartDto::enitityToDto)
        .toList();

    PageInfoDto pageInfo = PageInfoDto.builder()
        .totalPages(partVendorList.getTotalPages())
        .pageNumber(pageNo)
        .pageSize(pageSize)
        .totalRecords(partVendorList.getTotalElements())
        .build();

        return ApiPageResponseDto.<List<PartDto>>builder()
        .data(partDtos)
        .pageInfo(pageInfo)
        .build();
}
    

}
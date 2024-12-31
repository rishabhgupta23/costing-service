package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.Vendor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorDto {
    private Long id;
    private String name;
    private String address;
    private String emailId;
    private String contactNumber;

    public static VendorDto entityToDto(Vendor vendor) {
        return VendorDto.builder()
        .id(vendor.getVendorId())
        .name(vendor.getName())
        .emailId(vendor.getEmailId())
        .contactNumber(vendor.getContactNumber())
        .address(vendor.getAddress())
        .build();
    }
}

package com.jubeiwato.costing_service.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.VendorDto;

import com.jubeiwato.costing_service.services.VendorService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;




@CrossOrigin
@RestController
@RequestMapping("/vendors")
public class VendorController {
    
    
    private final VendorService vendorService;
    
    

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
        
    }

    @GetMapping()
    public ResponseEntity<ApiPageResponseDto<List<VendorDto>>> getVendorList( @RequestParam(required = false) String name,
    @RequestParam(required = false) String address,
    @RequestParam(required = false) String emailId,
    @RequestParam(required = false) String contactNumber,@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
    @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
            ApiPageResponseDto<List<VendorDto>> response = this.vendorService.getVendorList(name, address, emailId, contactNumber, pageNo, size);
            return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorDto> getVendorById(@PathVariable Long id) {
        VendorDto vendor = this.vendorService.getVendorById(id);
        return new ResponseEntity<>(vendor, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<GeneralResponseDto> createVendor(@RequestBody VendorDto vendorDto) {
        this.vendorService.createVendor(vendorDto.getName(), vendorDto.getEmailId(),
         vendorDto.getContactNumber(), vendorDto.getAddress());
         GeneralResponseDto response = new GeneralResponseDto("Vendor created successfully", HttpStatus.CREATED.value());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorDto> updateVendorById(@PathVariable Long id, @RequestBody VendorDto vendorDto) {
        VendorDto updatedVendor = this.vendorService.updateVendorById(id, vendorDto.getName(), vendorDto.getEmailId(),
         vendorDto.getContactNumber(), vendorDto.getAddress());
        return new ResponseEntity<>(updatedVendor, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponseDto> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendorById(id);
        GeneralResponseDto response = new GeneralResponseDto("Vendor deleted successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/parts") 
    public ResponseEntity<ApiPageResponseDto<List<PartDto>>> getVendorParts(    @PathVariable("id") Long vendorId,@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo, @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize ) {
        ApiPageResponseDto<List<PartDto>> response = vendorService.getVendorParts(vendorId, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }

}



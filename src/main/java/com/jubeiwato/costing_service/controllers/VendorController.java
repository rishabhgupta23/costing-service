package com.jubeiwato.costing_service.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.VendorDto;

import com.jubeiwato.costing_service.services.VendorService;

import org.springframework.web.bind.annotation.RequestMapping;

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




@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/vendors")
public class VendorController {
    
    
    private final VendorService vendorService;
    
    

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
        
    }

    @GetMapping()
    public ResponseEntity<List<VendorDto>> getVendorList() {
        List<VendorDto> vendorList = this.vendorService.getVendorList();
        return new ResponseEntity<>(vendorList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorDto> getVendorById(@PathVariable Long id) {
        VendorDto vendor = this.vendorService.getVendorById(id);
        return new ResponseEntity<>(vendor, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<String> createVendor(@RequestBody VendorDto vendorDto) {
        this.vendorService.createVendor(vendorDto.getName(), vendorDto.getEmailId(),
         vendorDto.getContactNumber(), vendorDto.getAddress());
        return new ResponseEntity<>("{\"message\": \"Successful\"}", HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorDto> updateVendorById(@PathVariable Long id, @RequestBody VendorDto vendorDto) {
        VendorDto updatedVendor = this.vendorService.updateVendorById(id, vendorDto.getName(), vendorDto.getEmailId(),
         vendorDto.getContactNumber(), vendorDto.getAddress());
        return new ResponseEntity<>(updatedVendor, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendorById(id);
        return ResponseEntity.ok("Vendor deleted successfully");
    }

    @GetMapping("/{id}/parts") 
public ResponseEntity<List<PartDto>> getVendorParts(@PathVariable("id") Long vendorId) {
    List<PartDto> parts = vendorService.getVendorParts(vendorId);
        return ResponseEntity.ok(parts);
   }

}



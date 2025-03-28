package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.constants.DateFormat;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.constants.FileExtension;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.entities.User;
import org.springframework.web.bind.annotation.RestController;

import com.jubeiwato.costing_service.constants.AppConstants;

import com.jubeiwato.costing_service.services.VendorService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/vendors")
public class VendorController {
    
    
    private final VendorService vendorService;
    

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiPageResponseDto<List<VendorDto>>> getVendorList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String emailId,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = "name") String sortColumn,
            @RequestParam(required = false, defaultValue = "ASC") Sorting sortMode,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();

        ApiPageResponseDto<List<VendorDto>> response = this.vendorService.getVendorList(companyId, name, address,
                emailId, contactNumber, pageNo, pageSize, sortColumn, sortMode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileResponseDto> downloadVendorListData(@AuthenticationPrincipal User authenticatedUser)
            throws IOException {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        byte[] excelBytes = vendorService.downloadVendorExcel(companyId);

        String base64Excel = Base64.getEncoder().encodeToString(excelBytes);

        String timestamp = new SimpleDateFormat(DateFormat.yyyyMMdd_HHmmss.getFormat()).format(new Date());
        String filename = "vendorList_" + timestamp + "."+ FileExtension.SPREADSHEET.getValue();

        FileResponseDto responseDto = FileResponseDto.builder()
                .fileData(base64Excel)
                .fileName(filename)
                .build();

        return ResponseEntity.ok()
                .body(responseDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VendorDto> getVendorById(@PathVariable Long id,@AuthenticationPrincipal User authenticatedUser ){
        VendorDto vendor = this.vendorService.getVendorById(id);
        return new ResponseEntity<>(vendor, HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MAINTAINER')")
    public ResponseEntity<GeneralResponseDto> createVendor(@RequestBody VendorDto vendorDto,@AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        vendorService.createVendor(companyId, vendorDto.getName(), vendorDto.getEmailId(),
               vendorDto.getContactNumber(), vendorDto.getAddress());
         GeneralResponseDto response = new GeneralResponseDto("Vendor created successfully", HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('MAINTAINER')")
    public ResponseEntity<VendorDto> updateVendorById(@PathVariable Long id, @RequestBody VendorDto vendorDto,
            @AuthenticationPrincipal User authenticatedUser) {
        VendorDto updatedVendor = this.vendorService.updateVendorById(id, vendorDto.getName(), vendorDto.getEmailId(),
         vendorDto.getContactNumber(), vendorDto.getAddress());
        return new ResponseEntity<>(updatedVendor, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('MAINTAINER')")
    public ResponseEntity<GeneralResponseDto> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendorById(id);
        GeneralResponseDto response = new GeneralResponseDto("Vendor deleted successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/parts") 
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiPageResponseDto<List<PartDto>>> getVendorParts(    @PathVariable("id") Long vendorId,@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo, @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize ) {
        ApiPageResponseDto<List<PartDto>> response = vendorService.getVendorParts(vendorId, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }

}



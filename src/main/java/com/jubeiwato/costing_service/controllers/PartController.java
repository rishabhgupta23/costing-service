package com.jubeiwato.costing_service.controllers;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.jubeiwato.costing_service.constants.*;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static com.jubeiwato.costing_service.constants.UserRoleConstants.*;
import com.jubeiwato.costing_service.services.PartService;
import java.io.IOException;


@CrossOrigin
@RestController
@RequestMapping("/parts")
public class PartController {

    private PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }


    @GetMapping()
    public ResponseEntity<ApiPageResponseDto<PartDataDto>> getParts(
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String partNumber,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String unit,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(defaultValue = "partNumber") String sortColumn,
            @RequestParam(defaultValue = "ASC") Sorting sortMode,
            @AuthenticationPrincipal User authenticatedUser 
    ) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();  
            PartDto filter = PartDto.builder()
                .partName(partName)
                .partNumber(partNumber)
                .categoryName(categoryName)
                .type(type)
                .unit(unit)
                .build();

        ApiPageResponseDto<PartDataDto> response = partService.getParts(filter,companyId, pageNo, pageSize, sortColumn, sortMode);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/types")
    public ResponseEntity<List<String>> getPartTypes() {
        return new ResponseEntity<>(partService.getPartTypes(), HttpStatus.OK);
    }

     @GetMapping("/units")
   public ResponseEntity<ApiPageResponseDto<List<PartUnitDto>>> getPartUnits(@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize) {
        ApiPageResponseDto<List<PartUnitDto>> response = partService.getPartUnits(pageNo, pageSize);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{partId}")
    public ResponseEntity<PartDto> getPartById(@PathVariable Long partId, @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        PartDto part = this.partService.getPartById(partId, companyId);
        return new ResponseEntity<>(part, HttpStatus.OK);
    }
    
    @PostMapping("/{partId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "') or hasRole('" + MAINTAINER + "')")
    public ResponseEntity<PartDto> updatePartById(@PathVariable Long partId, @RequestBody PartRequestDto request, @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        PartDto updatedPart = this.partService.updatePartById(partId, request, companyId);
        return new ResponseEntity<>(updatedPart, HttpStatus.OK);
    }

    @PostMapping 
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "') or hasRole('" + MAINTAINER + "')")
    public ResponseEntity<GeneralResponseDto> createPart(@RequestBody PartRequestDto request, @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        partService.createPart(request, companyId);
        GeneralResponseDto response = new GeneralResponseDto("Successful", HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{partId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "') or hasRole('" + MAINTAINER + "')")
    public ResponseEntity<GeneralResponseDto> deletePartById(@PathVariable Long partId,@AuthenticationPrincipal User authenticatedUser ) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        partService.deletePartById(partId, companyId);
        GeneralResponseDto response = new GeneralResponseDto("Part deleted successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/download")
    public ResponseEntity<FileResponseDto> exportPartsToExcel(@AuthenticationPrincipal User authenticatedUser) throws IOException{
            
            Long companyId = authenticatedUser.getCompany().getCompanyId(); 
            byte[] fileResponse = partService.downloadPartsToExcel(companyId);
            String base64Excel = Base64.getEncoder().encodeToString(fileResponse);

            String timestamp = new SimpleDateFormat(DateFormat.yyyyMMdd_HHmmss.getFormat()).format(new Date());
            String filename = "partList_" + timestamp + "."+ FileExtension.SPREADSHEET.getValue();

            FileResponseDto responseDto = FileResponseDto.builder()
                .fileData(base64Excel)
                .fileName(filename)
                .build();

                return ResponseEntity.ok()
                .body(responseDto);
    }

    @GetMapping("/bom/{parentPartId}/download")
    public ResponseEntity<FileResponseDto> exportBomPartListToExcel(@PathVariable Long parentPartId,@AuthenticationPrincipal User authenticatedUser) throws IOException {   
    Long companyId = authenticatedUser.getCompany().getCompanyId(); 
    FileResponseDto responseDto = partService.downloadBomPartListToExcel(parentPartId,companyId);
    return ResponseEntity.ok().body(responseDto);
    }

    @GetMapping("/cost-history")
    public ResponseEntity<CostHistoryResponseDto> getPartCostsByPartAndVendor(
            @RequestParam Long partId,
            @RequestParam Long vendorId, @AuthenticationPrincipal User authenticatedUser) {
            Long companyId = authenticatedUser.getCompany().getCompanyId();
        CostHistoryResponseDto response = partService.getPartCostsByPartAndVendor(partId, vendorId,companyId );
        return ResponseEntity.ok(response);
    }
}       

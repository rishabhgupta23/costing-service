package com.jubeiwato.costing_service.controllers;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.jubeiwato.costing_service.constants.*;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.entities.Part;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) PartType type,
            @RequestParam(required = false) String unit,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(defaultValue = "partName") String sortColumn,
            @RequestParam(defaultValue = "ASC") Sorting sortMode
    ) {
        Part filter = Part.builder()
                .partName(partName)
                .partNumber(partNumber)
                .categoryName(categoryName)
                .type(type)
                .unit(unit)
                .build();

        ApiPageResponseDto<PartDataDto> response = partService.getParts(filter, pageNo, pageSize, sortColumn, sortMode);
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

    @GetMapping("/cost-factors")
    public ResponseEntity<ApiPageResponseDto<List<CostFactorDto>>> getCostFactors(@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize) {
       ApiPageResponseDto<List<CostFactorDto>> response = partService.getCostFactors(pageNo, pageSize);
       return new ResponseEntity<>(response, HttpStatus.OK);
}

    @GetMapping("/{partId}")
    public ResponseEntity<PartDto> getPartById(@PathVariable Long partId) {
        PartDto part = this.partService.getPartById(partId);
        return new ResponseEntity<>(part, HttpStatus.OK);
    }
    
    @PostMapping("/{partId}")
    public ResponseEntity<PartDto> updatePartById(@PathVariable Long partId, @RequestBody PartRequestDto request) {
        PartDto updatedPart = this.partService.updatePartById(partId, request);
        return new ResponseEntity<>(updatedPart, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<GeneralResponseDto> createPart(@RequestBody PartRequestDto request) {
        partService.createPart(request);
        GeneralResponseDto response = new GeneralResponseDto("Successful", HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{partId}")
    public ResponseEntity<GeneralResponseDto> deletePartById(@PathVariable Long partId) {
        partService.deletePartById(partId);
        GeneralResponseDto response = new GeneralResponseDto("Part deleted successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/download")
    public ResponseEntity<FileResponseDto> exportPartsToExcel() throws IOException{

            byte[] fileResponse = partService.downloadPartsToExcel();
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
    public ResponseEntity<FileResponseDto> exportBomPartListToExcel(@PathVariable Long parentPartId) throws IOException {
    FileResponseDto responseDto = partService.downloadBomPartListToExcel(parentPartId);
    return ResponseEntity.ok().body(responseDto);
    }

    @GetMapping("/cost-history")
    public ResponseEntity<CostHistoryResponseDto> getPartCostsByPartAndVendor(
            @RequestParam Long partId,
            @RequestParam Long vendorId) {
        CostHistoryResponseDto response = partService.getPartCostsByPartAndVendor(partId, vendorId);
        return ResponseEntity.ok(response);
    }
}

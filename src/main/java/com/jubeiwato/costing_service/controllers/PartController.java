package com.jubeiwato.costing_service.controllers;

import java.util.List;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.entities.Part;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PartDataDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.dtos.PartUnitDto;
import com.jubeiwato.costing_service.services.PartService;


@CrossOrigin
@RestController
@RequestMapping("/parts")
public class PartController {

    private PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }


    @GetMapping()
    public ResponseEntity<ApiPageResponseDto<PartDataDto>> getParts(@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNumber, @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE + "") int pageSize) {
        ApiPageResponseDto<PartDataDto> response = partService.getParts(pageNumber, pageSize);
        return new ResponseEntity<>(response, HttpStatus.OK);
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

    @GetMapping("/filter")
    public ResponseEntity<List<Part>> filterParts(
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String partNumber,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) PartType type,
            @RequestParam(required = false) String unit
    ) {
        Part filter = new Part();
        filter.setPartName(partName);
        filter.setPartNumber(partNumber);
        filter.setCategoryName(categoryName);
        filter.setType(type);
        filter.setUnit(unit);

        List<Part> filteredParts = partService.getFilteredParts(filter);
        return ResponseEntity.ok(filteredParts);
    }

}

package com.jubeiwato.costing_service.controllers;

import java.util.List;

import com.jubeiwato.costing_service.dtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jubeiwato.costing_service.constants.AppConstants;
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
    public ResponseEntity<List<String>> getPartUnits() {
        return new ResponseEntity<>(partService.getPartUnits(), HttpStatus.OK);
    }

    @GetMapping("/cost-factors")
    public ResponseEntity<List<CostFactorDto>> getCostFactors() {
        return new ResponseEntity<>(partService.getCostFactors(), HttpStatus.OK);
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
        GeneralResponseDto response = new GeneralResponseDto("Successful", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{partId}")
    public ResponseEntity<GeneralResponseDto> deletePartById(@PathVariable Long partId) {
        partService.deletePartById(partId);
        GeneralResponseDto response = new GeneralResponseDto("Part deleted successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

}

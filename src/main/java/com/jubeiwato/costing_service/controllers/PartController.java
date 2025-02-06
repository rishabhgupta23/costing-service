package com.jubeiwato.costing_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.services.PartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@CrossOrigin
@RestController
@RequestMapping("/parts")
public class PartController {

    private PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }


    @GetMapping()
    public ResponseEntity<ApiPageResponseDto<PartDto>> getParts(@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNumber, @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE + "") int pageSize) {
        return new ResponseEntity<>(partService.getParts(pageNumber, pageSize), HttpStatus.OK);
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
    public ResponseEntity<PartDto> updatePartById(@PathVariable Long partId, @RequestBody PartDto partDto) {
        PartDto updatedPart = this.partService.updatePartById(partId, partDto.getPartName(),
         partDto.getType(), partDto.getUnit(), partDto.getCategoryName());
        return new ResponseEntity<>(updatedPart, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<String> createPart(@RequestBody PartRequestDto request) {
        this.partService.createPart(request);
        return new ResponseEntity<>("{\"message\": \"Successful\"}", HttpStatus.OK);
    }
    
}

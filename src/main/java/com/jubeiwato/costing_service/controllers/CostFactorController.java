package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.services.CostFactorService;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/cost-factors")
public class CostFactorController {

    private final CostFactorService costFactorService;

    public CostFactorController(CostFactorService costFactorService) {
        this.costFactorService = costFactorService;
    }

    @GetMapping
    public ResponseEntity<ApiPageResponseDto<List<CostFactorDto>>> getCostFactors(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false) String factorName,
            @RequestParam(required = false, defaultValue = "factorName") String sortColumn,
            @RequestParam(required = false, defaultValue = "ASC") Sorting sortMode,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        ApiPageResponseDto<List<CostFactorDto>> response = costFactorService.getCostFactors(pageNo, pageSize,companyId,factorName,sortColumn,sortMode);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<GeneralResponseDto> createCostFactor(@RequestParam String factorName, @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        costFactorService.createCostFactor(factorName, companyId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GeneralResponseDto("Cost Factor created successfully", HttpStatus.CREATED.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<CostFactorDto> updateCostFactor(@PathVariable Long id,@RequestParam String factorName, @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        CostFactorDto updated = costFactorService.updateCostFactor(id, factorName, companyId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<GeneralResponseDto> deleteCostFactor(@PathVariable Long id,
                                                               @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        costFactorService.deleteCostFactor(id, companyId);
        return ResponseEntity.ok(new GeneralResponseDto("Cost Factor deleted successfully", HttpStatus.OK.value()));
    }

}
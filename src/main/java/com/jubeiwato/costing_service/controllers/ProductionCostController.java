package com.jubeiwato.costing_service.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jubeiwato.costing_service.dtos.ProductionCostResponseDto;
import com.jubeiwato.costing_service.dtos.ProductionPlanRequestDto;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.services.ProductionCostService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/production/cost")
public class ProductionCostController {
    private final ProductionCostService productionCostService;

    public ProductionCostController(ProductionCostService productionCostService) {
        this.productionCostService = productionCostService;
    }

@PostMapping("/calculate")
public ProductionCostResponseDto calculateProductionCost(
        @RequestBody @Valid ProductionPlanRequestDto request,
        @AuthenticationPrincipal User authenticatedUser) {

    Long companyId = authenticatedUser.getCompany().getCompanyId();
    return productionCostService.calculateProductionCost(request.getParts(), request.getPriceMode(), companyId);
}
}

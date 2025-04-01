package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.dtos.CostCalcResultDto;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.services.CostCalcService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@CrossOrigin
@RestController
@RequestMapping("/cost")
public class CostController {
    private final CostCalcService costService;

    public CostController(CostCalcService costService) {
        this.costService = costService;
    }

    @GetMapping("/calculate/{partId}")
    public CostCalcResultDto calculateCost(@PathVariable Long partId, @RequestParam String priceMode, @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        return costService.calculatePrice(partId, priceMode, companyId);
    }

}

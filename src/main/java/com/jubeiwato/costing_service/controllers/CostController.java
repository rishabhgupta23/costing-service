package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.dtos.ResultCostDto;
import com.jubeiwato.costing_service.services.CostCalcService;
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
    public ResultCostDto calculateCost(@PathVariable Long partId, @RequestParam String priceMode) {
        return costService.calculatePrice(partId, priceMode);
    }

}

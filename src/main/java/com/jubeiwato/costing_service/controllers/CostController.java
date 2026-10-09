package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.dtos.CostCalcResultDto;
import com.jubeiwato.costing_service.dtos.FileResponseDto;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.services.CostCalcService;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.springframework.http.ResponseEntity;
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

@GetMapping("/calculate/{partId}/download")
public ResponseEntity<FileResponseDto> downloadCostExcel(
        @PathVariable Long partId,
        @RequestParam String priceMode,
        @AuthenticationPrincipal User authenticatedUser) throws IOException {

    Long companyId = authenticatedUser.getCompany().getCompanyId();

    byte[] fileResponse =
            costService.downloadCostExcel(partId, priceMode, companyId);

    String base64Excel = Base64.getEncoder().encodeToString(fileResponse);

    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    String filename = "costCalculation_" + timestamp + ".xlsx";

    FileResponseDto responseDto = FileResponseDto.builder()
            .fileData(base64Excel)
            .fileName(filename)
            .build();

    return ResponseEntity.ok(responseDto);
}

}

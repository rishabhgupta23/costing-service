package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.dtos.TemplateDto;
import com.jubeiwato.costing_service.dtos.TemplateRequestDto;
import com.jubeiwato.costing_service.dtos.TemplateResponseDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import com.jubeiwato.costing_service.services.TemplateService;
import com.jubeiwato.costing_service.entities.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<GeneralResponseDto> createTemplate(
            @RequestBody TemplateRequestDto templateDto,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        templateService.createTemplate(templateDto, companyId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GeneralResponseDto("Template created successfully", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<List<TemplateDto>> getAllTemplates(@AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        return ResponseEntity.ok(templateService.getAllTemplates(companyId));
    }


    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponseDto> getTemplateById(
            @PathVariable Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        return ResponseEntity.ok(templateService.getTemplateById(id, companyId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<TemplateResponseDto> updateTemplate(
            @PathVariable Long id,
            @RequestBody TemplateRequestDto templateDto,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        TemplateResponseDto updatedTemplate = templateService.updateTemplate(id, templateDto, companyId);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<GeneralResponseDto> deleteTemplate(
            @PathVariable Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        templateService.deleteTemplate(id, companyId);
        return ResponseEntity.ok(new GeneralResponseDto("Template deleted successfully", HttpStatus.OK.value()));
    }
}
package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.dtos.TemplateDto;
import com.jubeiwato.costing_service.dtos.TemplateRequestDto;
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

    // @PutMapping("/{templateId}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    // public ResponseEntity<TemplateDto> updateTemplate(
    //         @PathVariable Long templateId,
    //         @RequestBody TemplateRequestDto templateDto,
    //         @AuthenticationPrincipal User authenticatedUser) {
    //     Long companyId = authenticatedUser.getCompany().getCompanyId();
    //     TemplateDto updatedTemplate = templateService.updateTemplate(templateId, templateDto, companyId);
    //     return ResponseEntity.ok(updatedTemplate);
    // }

    // @DeleteMapping("/{templateId}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    // public ResponseEntity<GeneralResponseDto> deleteTemplate(
    //         @PathVariable Long templateId,
    //         @AuthenticationPrincipal User authenticatedUser) {
    //     Long companyId = authenticatedUser.getCompany().getCompanyId();
    //     templateService.deleteTemplate(templateId, companyId);
    //     return ResponseEntity.ok(new GeneralResponseDto("Template deleted successfully", HttpStatus.OK.value()));
    // }

    // @GetMapping
    // public ResponseEntity<List<TemplateDto>> getTemplates(@AuthenticationPrincipal User authenticatedUser) {
    //     Long companyId = authenticatedUser.getCompany().getCompanyId();
    //     List<TemplateDto> templates = templateService.getTemplates(companyId);
    //     return ResponseEntity.ok(templates);
    // }

    // @GetMapping("/{templateId}")
    // public ResponseEntity<TemplateDto> getTemplateById(
    //         @PathVariable Long templateId,
    //         @AuthenticationPrincipal User authenticatedUser) {
    //     Long companyId = authenticatedUser.getCompany().getCompanyId();
    //     TemplateDto template = templateService.getTemplateById(templateId, companyId);
    //     return ResponseEntity.ok(template);
    // }
}
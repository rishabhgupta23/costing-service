package com.jubeiwato.costing_service.controllers;

import java.util.List;
import static com.jubeiwato.costing_service.constants.UserRoleConstants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jubeiwato.costing_service.entities.User;

import com.jubeiwato.costing_service.dtos.CompanyDto;
import com.jubeiwato.costing_service.services.CompanyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping()
    public ResponseEntity<String> createCompany(@RequestBody CompanyDto companyDto) {
        companyService.createCompany(companyDto);
        return new ResponseEntity<>("Company created successfully!", HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping()
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long companyId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(companyService.getCompanyById(companyId, currentUser));
    }

    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
    @PutMapping("/{companyId}")
    public ResponseEntity<String> updateCompanybyId(@PathVariable Long companyId, @RequestBody CompanyDto companyDto,
            @AuthenticationPrincipal User currentUser) {
        companyService.updateCompanybyId(companyId, companyDto, currentUser);
        return new ResponseEntity<>("Company updated successfully!", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{companyId}")
    public ResponseEntity<String> deleteCompanybyId(@PathVariable Long companyId) {
        companyService.deleteCompanybyId(companyId);
        return new ResponseEntity<>("Company deleted successfully!", HttpStatus.OK);
    }
}

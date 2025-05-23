package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.entities.PartAttribute;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.services.PartAttributeService;
import static com.jubeiwato.costing_service.constants.UserRoleConstants.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/part-attributes")
@RequiredArgsConstructor
public class PartAttributeController {

    private final PartAttributeService partAttributeService;

    @PostMapping
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
    public ResponseEntity<GeneralResponseDto> createPartAttribute(@RequestBody PartAttributeDto dto,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        partAttributeService.createPartAttribute(dto, companyId);
        GeneralResponseDto response = new GeneralResponseDto("Successful", HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiPageResponseDto<List<PartAttributeDto>>> getPartAttributeList(
            @RequestParam(required = false) String attributeName,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "attributeName") String sortColumn,
            @RequestParam(defaultValue = "ASC") Sorting sortMode,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();

        ApiPageResponseDto<List<PartAttributeDto>> response = partAttributeService.getPartAttributeList(
                companyId, attributeName, pageNo, pageSize, sortColumn, sortMode);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{attributeId}")
    public ResponseEntity<PartAttributeDto> getPartAttributeById(@PathVariable Long attributeId,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        PartAttribute partAttribute = partAttributeService.getPartAttributeById(attributeId, companyId);
        PartAttributeDto partAttributeDto = PartAttributeDto.entityToDto(partAttribute);
        return ResponseEntity.ok(partAttributeDto);
    }

    @PutMapping("/{attributeId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
    public ResponseEntity<PartAttributeDto> updatePartAttributeById(@PathVariable Long attributeId,
            @RequestBody PartAttributeDto dto,
            @AuthenticationPrincipal User user) {
        Long companyId = user.getCompany().getCompanyId();
        PartAttribute updatedPartAttribute = partAttributeService.updatePartAttribute(attributeId, dto, companyId);
        PartAttributeDto updatedPartAttributeDto = PartAttributeDto.entityToDto(updatedPartAttribute);
        return ResponseEntity.ok(updatedPartAttributeDto);
    }

    @DeleteMapping("/{attributeId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "') ")
    public ResponseEntity<GeneralResponseDto> deletePartAttribute(@PathVariable Long attributeId,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        partAttributeService.deletePartAttribute(attributeId, companyId);
        GeneralResponseDto response = new GeneralResponseDto("PART-ATTRIBUTE deleted successfully",
                HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

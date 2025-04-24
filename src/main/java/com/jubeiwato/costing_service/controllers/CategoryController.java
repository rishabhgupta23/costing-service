package com.jubeiwato.costing_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.jubeiwato.costing_service.constants.UserRoleConstants.*;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CategoryDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@CrossOrigin
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
public ResponseEntity<ApiPageResponseDto<List<CategoryDto>>> getCategoryList(
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
        @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
        @RequestParam(defaultValue = "name") String sortColumn,
        @RequestParam(defaultValue = "ASC") Sorting sortMode,
        @AuthenticationPrincipal User authenticatedUser
) {
    Long companyId = authenticatedUser.getCompany().getCompanyId();

    ApiPageResponseDto<List<CategoryDto>> response = categoryService.getCategoryList(
            companyId, name, pageNo, pageSize, sortColumn, sortMode);

    return ResponseEntity.ok(response);
}

    @PostMapping()
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "') or hasRole('" + MAINTAINER + "')")
    public ResponseEntity<GeneralResponseDto> createCategory(@Valid @RequestBody CategoryDto request, @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        this.categoryService.createCategory(request.getName(),companyId);
        GeneralResponseDto response = new GeneralResponseDto("Successful", HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    }

      @PutMapping("/{categoryId}")
      @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
      public ResponseEntity<CategoryDto> updateCategoryById(@PathVariable Long categoryId, @RequestBody CategoryDto request, @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        CategoryDto updatedUser = this.categoryService.updateCategoryById(categoryId, request, companyId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
    public ResponseEntity<GeneralResponseDto> deleteCategoryById(@PathVariable Long categoryId,@AuthenticationPrincipal User authenticatedUser ) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        categoryService.deleteCategoryById(categoryId, companyId);
        GeneralResponseDto response = new GeneralResponseDto("Category deleted successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

}

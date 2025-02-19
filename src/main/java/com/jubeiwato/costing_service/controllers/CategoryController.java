package com.jubeiwato.costing_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CategoryDto;
import com.jubeiwato.costing_service.entities.Category;
import com.jubeiwato.costing_service.services.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@CrossOrigin
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping()
    public ResponseEntity<ApiPageResponseDto<List<CategoryDto>>> getCategoryList( @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
      ApiPageResponseDto<List<CategoryDto>> response = categoryService.getCategoryList(page, size);
      return ResponseEntity.ok(response);
}

    @PostMapping()
    public ResponseEntity<String> postMethodName(@RequestBody CategoryDto request) {
        this.categoryService.createCategory(request.getName());
        return new ResponseEntity<>("{\"message\": \"Successful\"}", HttpStatus.OK);
        
    }
}

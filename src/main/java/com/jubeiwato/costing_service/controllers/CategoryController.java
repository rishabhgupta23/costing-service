package com.jubeiwato.costing_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<String>> getCategoryList() {
        return new ResponseEntity<>(categoryService.getCategoryList(), HttpStatus.OK); 
    }

    @PostMapping()
    public ResponseEntity<String> postMethodName(@RequestBody CategoryDto request) {
        this.categoryService.createCategory(request.getName());
        return new ResponseEntity<>("{\"message\": \"Successful\"}", HttpStatus.OK);
        
    }
}

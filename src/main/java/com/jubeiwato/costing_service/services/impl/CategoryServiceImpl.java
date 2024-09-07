package com.jubeiwato.costing_service.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.entities.Category;
import com.jubeiwato.costing_service.repositories.CategoryRepository;
import com.jubeiwato.costing_service.services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

        
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<String> getCategoryList() {
        return categoryRepository.findAll().stream().map(Category::getName).toList();
    }

    @Override
    public void createCategory(String categoryName) {
        Category category = Category.builder().name(categoryName).build();
        categoryRepository.save(category);
    }

    @Override
    public void updateCategory(Long id, String newCategoryName) {
        Category category = categoryRepository.getReferenceById(id);
        category.setName(newCategoryName);
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.getReferenceById(id);
        category.setDeleteFlag(DeleteFlag.POSTITVE.getValue());
        categoryRepository.save(category);
    }
}

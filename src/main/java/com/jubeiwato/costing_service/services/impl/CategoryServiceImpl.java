package com.jubeiwato.costing_service.services.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CategoryDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
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
       public ApiPageResponseDto<List<CategoryDto>> getCategoryList(int pageNo, int size) {
        Pageable pageable = PageRequest.of(pageNo, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

          List<CategoryDto> categoryDtos = categoryPage.getContent().stream()
                .map(CategoryDto::entityToDto)
                .toList();
   
        PageInfoDto pageInfo = PageInfoDto.builder()
                .pageNumber(pageNo)
                .pageSize(size)
                .totalPages(categoryPage.getTotalPages())
                .totalRecords(categoryPage.getTotalElements())
                .build();

        return ApiPageResponseDto.<List<CategoryDto>>builder()
                .data(categoryDtos)
                .pageInfo(pageInfo)
                .build();
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

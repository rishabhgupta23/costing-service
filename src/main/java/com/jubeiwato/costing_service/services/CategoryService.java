package com.jubeiwato.costing_service.services;

import java.util.List;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CategoryDto;

public interface CategoryService {
    ApiPageResponseDto<List<CategoryDto>> getCategoryList(int pageNo, int pageSize);

    void createCategory(String categoryName);

    void updateCategory(Long id, String newCategoryName);

    void deleteCategory(Long id);
}

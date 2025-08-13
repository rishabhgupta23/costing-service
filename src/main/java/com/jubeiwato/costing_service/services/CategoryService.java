package com.jubeiwato.costing_service.services;

import java.util.List;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CategoryDto;

public interface CategoryService {
    ApiPageResponseDto<List<CategoryDto>> getCategoryList(Long companyId, String name, int pageNo, int pageSize, String sortColumn, Sorting sortMode);

    void createCategory(String categoryName, Long companyId);

    CategoryDto updateCategoryById(Long categoryid, CategoryDto request, Long companyId);

    void deleteCategoryById(Long categoryId, Long companyId);
}

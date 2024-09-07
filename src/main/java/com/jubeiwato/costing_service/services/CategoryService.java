package com.jubeiwato.costing_service.services;

import java.util.List;

public interface CategoryService {
    List<String> getCategoryList();

    void createCategory(String categoryName);

    void updateCategory(Long id, String newCategoryName);

    void deleteCategory(Long id);
}

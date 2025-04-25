package com.jubeiwato.costing_service.services.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CategoryDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.entities.Category;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.repositories.CategoryRepository;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.services.CategoryService;
import com.jubeiwato.costing_service.services.CategorySpecification;
import com.jubeiwato.costing_service.utils.ValidationUtil;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;

        
    public CategoryServiceImpl(CategoryRepository categoryRepository, CompanyRepository companyRepository) {
        this.categoryRepository = categoryRepository;
        this.companyRepository = companyRepository;
    }

    private Category getValidatedCategory(Long categoryId, Long companyId) {
        return categoryRepository
                .findByCategoryIdAndCompany_CompanyId(categoryId, companyId)
                .orElseThrow(() ->new AppException(
                        ErrorMessageConstant.CATEGORY_DOES_NOT_EXIST,HttpStatus.NOT_FOUND));
    }

    private void validateUniqueCategoryName(String name, Long companyId) {
        boolean exists = categoryRepository
            .existsByCompany_CompanyIdAndName(companyId, name);
            if (exists) {
                String msg = ErrorMessageConstant.getFormattedMessage(
                    ErrorMessageConstant.CATEGORY_ALREADY_EXISTS_TEMPLATE, name);
                throw new AppException(msg, HttpStatus.CONFLICT);
            }
    }

    
    @Override
    public ApiPageResponseDto<List<CategoryDto>> getCategoryList(Long companyId, String name, int pageNo, int pageSize, String sortColumn, Sorting sortMode) {

        if (!ValidationUtil.isValidInput(name)) {
            throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        Sort.Direction direction = (sortMode == Sorting.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortColumn);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Category> spec = new CategorySpecification(companyId, name);

        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

        List<CategoryDto> categoryDtos = categoryPage.getContent().stream()
                .map(CategoryDto::entityToDto)
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
                .totalPages(categoryPage.getTotalPages())
                .pageNumber(pageNo)
                .pageSize(pageSize)
                .totalRecords(categoryPage.getTotalElements())
                .build();

        return ApiPageResponseDto.<List<CategoryDto>>builder()
                .data(categoryDtos)
                .pageInfo(pageInfo)
                .build();
    }

    @Override
    public void createCategory(String categoryName, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.INVALID_COMPANY, HttpStatus.BAD_REQUEST));
        validateUniqueCategoryName(categoryName, companyId);

        Category category = Category.builder().name(categoryName).company(company).build();
        categoryRepository.save(category);
    }

    @Override
    public CategoryDto updateCategoryById(Long categoryId, CategoryDto request, Long companyId) {
        Category category = getValidatedCategory(categoryId, companyId);
        String newName = request.getName();
        if (!category.getName().equals(newName)) {
            validateUniqueCategoryName(newName, companyId);
            category.setName(newName);
        }
        Category updated = categoryRepository.save(category);
        return CategoryDto.entityToDto(updated);
    }

    @Override
    public void deleteCategoryById(Long categoryId, Long companyId) {
        Category category = getValidatedCategory(categoryId, companyId);
        categoryRepository.delete(category);
    }
}

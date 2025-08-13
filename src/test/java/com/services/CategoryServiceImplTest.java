package com.services;

import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CategoryDto;
import com.jubeiwato.costing_service.entities.Category;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.repositories.CategoryRepository;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.services.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Company mockCompany(Long companyId) {
        Company company = Company.builder().companyId(companyId).build();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        return company;
    }

    private Category mockCategory(Long categoryId, String name, Long companyId) {
        return Category.builder()
                .categoryId(categoryId)
                .categoryName(name)
                .company(Company.builder().companyId(companyId).build())
                .build();
    }

    private CategoryDto buildDto(String name) {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryName(name);
        return dto;
    }

    @Test
    void testCreateCategory_CompanyNotFound() {
        when(companyRepository.findById(123L)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class,() -> 
        categoryService.createCategory("TestCategory", 123L));
         assertEquals(ErrorMessageConstant.INVALID_COMPANY, ex.getMessage());
    }

    @Test
    void testGetCategoryList_WithValidDataAndDescSort() {
        testCategoryListSorting(Sorting.DESC);
    }

    private void testCategoryListSorting(Sorting sortMode) {
        Long companyId = 1L;
        String name = "TestCategory";
        Page<Category> page = new PageImpl<>(List.of(mockCategory(1L, name, companyId)));

        when(categoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        ApiPageResponseDto<List<CategoryDto>> response = categoryService.getCategoryList(
                companyId, name, 0, 5, "name", sortMode);

        assertNotNull(response);
        assertEquals(1, response.getData().size());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        if (sortMode == Sorting.ASC)
            assertTrue(sort.getOrderFor("name").isAscending());
        else
        assertTrue(sort.getOrderFor("name").isDescending());
    }
    @Test
    void testGetCategoryList_WithAscendingSort() {
        testCategoryListSorting(Sorting.ASC);
    }

    @Test
    void testGetCategoryList_InvalidInput_ThrowsException() {
        AppException ex = assertThrows(AppException.class, () ->
                categoryService.getCategoryList(1L, "*", 0, 5, "name", Sorting.ASC));
        assertEquals(ErrorMessageConstant.INVALID_INPUT, ex.getMessage());
    }

    @Test
    void testUpdateCategoryById_NotFound() {
        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(99L, 1L))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                categoryService.updateCategoryById(99L, new CategoryDto(), 1L));
        assertEquals(ErrorMessageConstant.CATEGORY_DOES_NOT_EXIST, ex.getMessage());
    }

    @Test
    void testUpdateCategoryById_Success() {
        Long categoryId = 1L, companyId = 1L;
        String newName = "Updated";
        Category existing = mockCategory(categoryId, "Old", companyId);

        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.updateCategoryById(categoryId, buildDto(newName), companyId);

        assertEquals(newName, result.getCategoryName());
        verify(categoryRepository).save(any(Category.class));
    }


    @Test
    void testDeleteCategoryById_NotFound() {
        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(99L, 1L))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                categoryService.deleteCategoryById(99L, 1L));
        assertEquals(ErrorMessageConstant.CATEGORY_DOES_NOT_EXIST, ex.getMessage());
    }     
    @Test
    void testCreateCategory_Success() {
        Long companyId = 1L;
        mockCompany(companyId);
        categoryService.createCategory("Stationary", companyId);
        verify(categoryRepository).save(any(Category.class));
    }
    
    @Test
    void testDeleteCategoryById_Success() {
        Long categoryId = 1L, companyId = 1L;
        Category existing = mockCategory(categoryId, "X", companyId);
        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
                .thenReturn(Optional.of(existing));

        categoryService.deleteCategoryById(categoryId, companyId);
        verify(categoryRepository).delete(existing);
    }
    @Test
    void testCreateCategory_DuplicateName_ThrowsConflict() {
        Long companyId = 1L;
        String dupName = "hello";
        mockCompany(companyId);
        when(categoryRepository.existsByCompany_CompanyIdAndCategoryName(companyId, dupName)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () ->
                categoryService.createCategory(dupName, companyId));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }
    @Test
    void testUpdateCategory_SameName_NoDuplicateCheck() {
        Long categoryId = 10L, companyId = 1L;
        String sameName = "same";
        Category existing = mockCategory(categoryId,sameName,companyId);

        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);
        CategoryDto result = categoryService.updateCategoryById(categoryId, buildDto(sameName), companyId);

        verify(categoryRepository, never()).existsByCompany_CompanyIdAndCategoryName(anyLong(), anyString());
        assertEquals(sameName, result.getCategoryName());
    }

    // 3) updateCategoryById → new name different but duplicate → throw CONFLICT
    @Test
    void testUpdateCategory_DuplicateName_ThrowsConflict() {
        Long categoryId = 20L, companyId = 1L;
        Category existing = mockCategory(categoryId, "old", companyId);
        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.existsByCompany_CompanyIdAndCategoryName(companyId, "new"))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class, () ->
                categoryService.updateCategoryById(categoryId, buildDto("new"), companyId));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void testDeleteCategory_CompanyInvalid_ThrowsBadRequest() {
        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(5L, 999L))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                categoryService.deleteCategoryById(5L, 999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(ErrorMessageConstant.CATEGORY_DOES_NOT_EXIST.formatted(5L), ex.getMessage());
    }


}

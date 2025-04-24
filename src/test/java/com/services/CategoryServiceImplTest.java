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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void testCreateCategory_CompanyNotFound() {
        Long companyId = 123L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                categoryService.createCategory("TestCategory", companyId));

        assertEquals(ErrorMessageConstant.INVALID_COMPANY, exception.getMessage());
    }

    @Test
    void testGetCategoryList_WithValidDataAndDescSort() {
        Long companyId = 1L;
        String name = "TestCategory";
        int pageNo = 0, pageSize = 5;
        String sortColumn = "name";
        Sorting sortMode = Sorting.DESC;

        Category category = Category.builder().name(name).company(Company.builder().companyId(companyId).build()).build();
        Page<Category> categoryPage = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(categoryPage);

        ApiPageResponseDto<List<CategoryDto>> response = categoryService.getCategoryList(
                companyId, name, pageNo, pageSize, sortColumn, sortMode);

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(name, response.getData().get(0).getName());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        assertTrue(sort.getOrderFor(sortColumn).isDescending());
    }
    @Test
    void testGetCategoryList_WithAscendingSort() {
    Long companyId = 1L;
    String name = "TestCategory";
    int pageNo = 0;
    int pageSize = 5;
    String sortColumn = "name";
    Sorting sortMode = Sorting.ASC; // ASC instead of DESC

    Category category = Category.builder().name(name).company(Company.builder().companyId(companyId).build()).build();
    Page<Category> categoryPage = new PageImpl<>(List.of(category));

    when(categoryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(categoryPage);

    ApiPageResponseDto<List<CategoryDto>> response = categoryService.getCategoryList(companyId, name, pageNo, pageSize, sortColumn, sortMode);

    assertNotNull(response);
    assertEquals(1, response.getData().size());

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(categoryRepository).findAll(any(Specification.class), pageableCaptor.capture());
    Sort sort = pageableCaptor.getValue().getSort();
    assertTrue(sort.getOrderFor(sortColumn).isAscending()); // check ASC now
}


    @Test
    void testGetCategoryList_InvalidInput_ThrowsException() {
    Long companyId = 1L;
    String invalidName = "*"; 
    int pageNo = 0;
    int pageSize = 5;
    String sortColumn = "name";
    Sorting sortMode = Sorting.ASC;

    AppException exception = assertThrows(AppException.class, () ->
        categoryService.getCategoryList(companyId, invalidName, pageNo, pageSize, sortColumn, sortMode)
    );

    assertEquals(ErrorMessageConstant.INVALID_INPUT, exception.getMessage());
}

    @Test
    void testUpdateCategoryById_NotFound() {
        Long categoryId = 99L;
        Long companyId = 1L;
        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                categoryService.updateCategoryById(categoryId, new CategoryDto(), companyId));

        assertEquals(ErrorMessageConstant.CATEGORY_DOES_NOT_EXIST, exception.getMessage());
    }

    @Test
    void testUpdateCategoryById_Success() {
       Long categoryId = 1L;
       Long companyId = 1L;
       String oldName = "Old Category";
       String newName = "Updated Category";

    CategoryDto requestDto = new CategoryDto();
    requestDto.setName(newName);

    Category existingCategory = Category.builder()
            .categoryId(categoryId)
            .name(oldName)
            .company(Company.builder().companyId(companyId).build())
            .build();

    Category updatedCategory = Category.builder()
            .categoryId(categoryId)
            .name(newName)
            .company(Company.builder().companyId(companyId).build())
            .build();

    when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
            .thenReturn(Optional.of(existingCategory));
    when(categoryRepository.save(existingCategory)).thenReturn(updatedCategory);

    // Act
    CategoryDto result = categoryService.updateCategoryById(categoryId, requestDto, companyId);

    // Assert
    assertNotNull(result);
    assertEquals(newName, result.getName());

    verify(categoryRepository).findByCategoryIdAndCompany_CompanyId(categoryId, companyId);
    verify(categoryRepository).save(existingCategory);
}


    @Test
    void testDeleteCategoryById_NotFound() {
        Long categoryId = 99L;
        Long companyId = 1L;
        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                categoryService.deleteCategoryById(categoryId, companyId));

        assertEquals(ErrorMessageConstant.CATEGORY_DOES_NOT_EXIST, exception.getMessage());
    }

    @Test
    void testCreateCategory_Success() {
        Long companyId = 1L;
        Company company = Company.builder().companyId(companyId).build();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        categoryService.createCategory("Stationary", companyId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testDeleteCategoryById_Success() {
       Long categoryId = 1L;
       Long companyId = 1L;

    Category existingCategory = Category.builder()
            .categoryId(categoryId)
            .company(Company.builder().companyId(companyId).build())
            .build();

    when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
            .thenReturn(Optional.of(existingCategory));

    categoryService.deleteCategoryById(categoryId, companyId);

    verify(categoryRepository).delete(existingCategory);
}
    
// 1) createCategory → duplicate name → should throw CONFLICT
    @Test
    void testCreateCategory_DuplicateName_ThrowsConflict() {
        Long companyId = 1L;
        String dupName = "hello";
        // mock company exists
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(Company.builder().companyId(companyId).build()));
        // mock existsBy… returns true
        when(categoryRepository.existsByCompany_CompanyIdAndName(companyId, dupName))
            .thenReturn(true);

        AppException ex = assertThrows(AppException.class, () ->
            categoryService.createCategory(dupName, companyId)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains(dupName));
    }

    @Test
    void testUpdateCategory_SameName_NoDuplicateCheck() {
        Long categoryId = 10L, companyId = 1L;
        String sameName = "same";
        Category existing = Category.builder()
                .categoryId(categoryId)
                .name(sameName)
                .company(Company.builder().companyId(companyId).build())
                .build();

        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
            .thenReturn(Optional.of(existing));
        
        when(categoryRepository.save(existing)).thenReturn(existing);
        CategoryDto dto = new CategoryDto(); 
        dto.setName(sameName);
        CategoryDto result = categoryService.updateCategoryById(categoryId, dto, companyId);

        // verify save and that no existsBy… was invoked
        verify(categoryRepository, times(1)).save(existing);
        verify(categoryRepository, never()).existsByCompany_CompanyIdAndName(anyLong(), anyString());
        assertEquals(sameName, result.getName());
    }

    // 3) updateCategoryById → new name different but duplicate → throw CONFLICT
    @Test
    void testUpdateCategory_DuplicateName_ThrowsConflict() {
        Long categoryId = 20L, companyId = 1L;
        Category existing = Category.builder()
                .categoryId(categoryId)
                .name("old")
                .company(Company.builder().companyId(companyId).build())
                .build();

        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
            .thenReturn(Optional.of(existing));
        when(categoryRepository.existsByCompany_CompanyIdAndName(companyId, "new"))
            .thenReturn(true);

        CategoryDto dto = new CategoryDto(); dto.setName("new");
        AppException ex = assertThrows(AppException.class, () ->
            categoryService.updateCategoryById(categoryId, dto, companyId)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void testDeleteCategory_CompanyInvalid_ThrowsBadRequest() {
        Long categoryId = 5L, companyId = 999L;
    
        when(categoryRepository.findByCategoryIdAndCompany_CompanyId(categoryId, companyId))
            .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
            categoryService.deleteCategoryById(categoryId, companyId)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(ErrorMessageConstant.CATEGORY_DOES_NOT_EXIST.formatted(categoryId),
                     ex.getMessage());
    }


}

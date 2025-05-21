package com.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.entities.*;
import com.jubeiwato.costing_service.repositories.*;
import com.jubeiwato.costing_service.services.impl.CostFactorServiceImpl;

@ExtendWith(MockitoExtension.class)
 class CostFactorServiceImplTest {

    @Mock
    private CostFactorRepository costFactorRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CostFactorServiceImpl costFactorService;

    private static final Long COMPANY_ID = 1L;
    private static final String FACTOR_NAME = "Labor";

    private Company createCompany() {
        return Company.builder().companyId(COMPANY_ID).build();
    }

    private CostFactor createCostFactor(String name, int deleteFlag) {
        CostFactor costFactor = CostFactor.builder()
            .factorId(1L)
            .factorName(name)
            .build();
        costFactor.setDeleteFlag(deleteFlag);  // manually set inherited field
        return costFactor;
    }

    @Test
    void testGetCostFactors_InvalidInput() {
        assertThrows(Exception.class, () -> costFactorService.getCostFactors(0, 10, COMPANY_ID, " ", "name", Sorting.ASC));
    }

    @Test
    void testGetCostFactors_Success() {
        CostFactor costFactor = createCostFactor(FACTOR_NAME, DeleteFlag.NEGATIVE.getValue());
        Page<CostFactor> page = new PageImpl<>(List.of(costFactor));

        when(costFactorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ApiPageResponseDto<List<CostFactorDto>> response = costFactorService.getCostFactors(0, 10, COMPANY_ID, FACTOR_NAME, "name", Sorting.ASC);

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(FACTOR_NAME, response.getData().get(0).getName());
    }

    @Test
    void testCreateCostFactor_CompanyNotFound() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> costFactorService.createCostFactor(FACTOR_NAME, COMPANY_ID));

        assertEquals(ErrorMessageConstant.INVALID_COMPANY, exception.getMessage());
    }

    @Test
    void testCreateCostFactor_AlreadyExistsActive() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(createCompany()));
        when(costFactorRepository.findByCompany_CompanyIdAndFactorNameIgnoreCase(COMPANY_ID, FACTOR_NAME))
                .thenReturn(Optional.of(createCostFactor(FACTOR_NAME, DeleteFlag.NEGATIVE.getValue())));

        AppException ex = assertThrows(AppException.class, () -> costFactorService.createCostFactor(FACTOR_NAME, COMPANY_ID));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testCreateCostFactor_SoftDeleted_Restore() {
        CostFactor softDeleted = createCostFactor(FACTOR_NAME, DeleteFlag.POSITIVE.getValue()); // Initially deleted
    
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(createCompany()));
        when(costFactorRepository.findByCompany_CompanyIdAndFactorNameIgnoreCase(COMPANY_ID, FACTOR_NAME))
            .thenReturn(Optional.of(softDeleted));
    
        costFactorService.createCostFactor(FACTOR_NAME, COMPANY_ID);
    
        assertEquals(DeleteFlag.NEGATIVE.getValue(), softDeleted.getDeleteFlag(), "Expected cost factor to be restored (deleteFlag=0)");
    
        verify(costFactorRepository).save(softDeleted);
    }
    

    @Test
    void testCreateCostFactor_New() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(createCompany()));
        when(costFactorRepository.findByCompany_CompanyIdAndFactorNameIgnoreCase(COMPANY_ID, FACTOR_NAME)).thenReturn(Optional.empty());

        costFactorService.createCostFactor(FACTOR_NAME, COMPANY_ID);
        verify(costFactorRepository).save(any(CostFactor.class));
    }
//
    @Test
    void testUpdateCostFactor_ConflictExists() {
        CostFactor existing = createCostFactor("Material", DeleteFlag.NEGATIVE.getValue());
        CostFactor conflict = createCostFactor(FACTOR_NAME, DeleteFlag.NEGATIVE.getValue());

        when(costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(1L, COMPANY_ID, DeleteFlag.NEGATIVE.getValue()))
                .thenReturn(Optional.of(existing));
        when(costFactorRepository.findByCompany_CompanyIdAndFactorNameIgnoreCase(COMPANY_ID, FACTOR_NAME))
                .thenReturn(Optional.of(conflict));

        AppException ex = assertThrows(AppException.class, () -> costFactorService.updateCostFactor(1L, FACTOR_NAME, COMPANY_ID));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testUpdateCostFactor_Success() {
        CostFactor existing = createCostFactor("OldName", DeleteFlag.NEGATIVE.getValue());

        when(costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(1L, COMPANY_ID, DeleteFlag.NEGATIVE.getValue()))
                .thenReturn(Optional.of(existing));
        when(costFactorRepository.findByCompany_CompanyIdAndFactorNameIgnoreCase(COMPANY_ID, "NewName"))
                .thenReturn(Optional.empty());

        CostFactorDto updated = costFactorService.updateCostFactor(1L, "NewName", COMPANY_ID);

        assertEquals("NewName", updated.getName());
        verify(costFactorRepository).save(existing);
    }

    @Test
    void testDeleteCostFactor_AlreadyDeleted() {
        when(costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(1L, COMPANY_ID, DeleteFlag.NEGATIVE.getValue()))
                .thenThrow(new AppException(ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));
        assertThrows(AppException.class, () -> costFactorService.deleteCostFactor(1L, COMPANY_ID));
    }

    @Test
    void testDeleteCostFactor_Success() {
        CostFactor existing = createCostFactor(FACTOR_NAME, DeleteFlag.NEGATIVE.getValue());
        when(costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(
                1L, COMPANY_ID, DeleteFlag.NEGATIVE.getValue()))
            .thenReturn(Optional.of(existing));
    
        // now that the service returns void, we just invoke:
        costFactorService.deleteCostFactor(1L, COMPANY_ID);
    
        // verify that we marked it deleted
        assertEquals(DeleteFlag.POSITIVE.getValue(), existing.getDeleteFlag());
        verify(costFactorRepository).save(existing);
    }

    @Test
    void testDeleteCostFactor_AlreadySoftDeleted() {
        CostFactor softDeleted = createCostFactor(FACTOR_NAME, DeleteFlag.POSITIVE.getValue());
        when(costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(
                1L, COMPANY_ID, DeleteFlag.NEGATIVE.getValue()))
            .thenReturn(Optional.of(softDeleted));
    
        // should not throw, nor should it call save()
        costFactorService.deleteCostFactor(1L, COMPANY_ID);
        verify(costFactorRepository, never()).save(any());
    }

    @Test
   void testCreateCostFactor_InvalidName_ThrowsException() {
    when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(createCompany()));

    AppException exception = assertThrows(AppException.class, () ->
        costFactorService.createCostFactor("   ", COMPANY_ID) // Only spaces = empty after trim
    );

    assertEquals(ErrorMessageConstant.INVALID_COST_FACTOR, exception.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
 }

@Test
void testCreateCostFactor_NullName_ThrowsException() {
    when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(createCompany()));

    AppException exception = assertThrows(AppException.class, () ->
        costFactorService.createCostFactor(null, COMPANY_ID)
    );

    assertEquals(ErrorMessageConstant.INVALID_COST_FACTOR, exception.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
}
}
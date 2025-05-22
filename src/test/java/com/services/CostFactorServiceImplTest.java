// package com.services;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import java.util.Collections;
// import java.util.List;
// import java.util.Optional;

// import com.jubeiwato.costing_service.authentication.config.AppException;
// import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
// import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
// import com.jubeiwato.costing_service.dtos.CostFactorDto;
// import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
// import com.jubeiwato.costing_service.entities.Company;
// import com.jubeiwato.costing_service.entities.CostFactor;
// import com.jubeiwato.costing_service.repositories.CompanyRepository;
// import com.jubeiwato.costing_service.repositories.CostFactorRepository;
// import com.jubeiwato.costing_service.services.impl.CostFactorServiceImpl;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.Pageable;

// class CostFactorServiceImplTest {

//     @Mock
//     private CostFactorRepository costFactorRepository;

//     @Mock
//     private CompanyRepository companyRepository;

//     @InjectMocks
//     private CostFactorServiceImpl costFactorService;

//     private static final Long COMPANY_ID = 1L;
//     private static final Long FACTOR_ID = 1L;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     private Company mockCompany(Long companyId) {
//         Company company = Company.builder().companyId(companyId).build();
//         when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
//         return company;
//     }

//     private CostFactor mockCostFactor(Long factorId, String name, Long companyId) {
//         return CostFactor.builder()
//                 .factorId(factorId)
//                 .factorName(name)
//                 .company(Company.builder().companyId(companyId).build())
//                 .build();
//     }

//     private CostFactorDto buildCostFactorDto(String name) {
//         return CostFactorDto.builder().name(name).build();
//     }

//     @Test
//     void testGetCostFactors_Success() {
//         CostFactor costFactor = mockCostFactor(FACTOR_ID, "Test Factor", COMPANY_ID);
//         Page<CostFactor> page = new PageImpl<>(Collections.singletonList(costFactor));
//         when(costFactorRepository.findByCompany_CompanyIdAndDeleteFlag(eq(COMPANY_ID), eq(0), any(Pageable.class)))
//                 .thenReturn(page);

//         ApiPageResponseDto<List<CostFactorDto>> response = costFactorService.getCostFactors(0, 10, COMPANY_ID);

//         assertThat(response.getData()).hasSize(1);
//         assertThat(response.getData().get(0).getName()).isEqualTo("Test Factor");
//     }

//     @Test
//     void testCreateCostFactor_Success() {
//         mockCompany(COMPANY_ID);
//         CostFactorDto dto = buildCostFactorDto("New Factor");

//         when(costFactorRepository.existsByCompany_CompanyIdAndFactorNameAndDeleteFlag(eq(COMPANY_ID), eq("New Factor"), eq(0)))
//                 .thenReturn(false);

//         costFactorService.createCostFactor(dto, COMPANY_ID);

//         verify(costFactorRepository).save(any(CostFactor.class));
//     }

//     @Test
//     void testCreateCostFactor_CompanyNotFound() {
//         when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());
//         CostFactorDto dto = buildCostFactorDto("New Factor");

//         AppException exception = assertThrows(AppException.class, () -> costFactorService.createCostFactor(dto, COMPANY_ID));
 
//         assertThat(exception.getMessage()).isEqualTo(ErrorMessageConstant.INVALID_COMPANY);
//     }

//     @Test
//     void testCreateCostFactor_NameAlreadyExists() {
//         mockCompany(COMPANY_ID);
//         CostFactorDto dto = buildCostFactorDto("Duplicate Factor");

//         when(costFactorRepository.existsByCompany_CompanyIdAndFactorNameAndDeleteFlag(eq(COMPANY_ID), eq("Duplicate Factor"), eq(0)))
//                 .thenReturn(true);

//         AppException exception = assertThrows(AppException.class, () -> costFactorService.createCostFactor(dto, COMPANY_ID));

//         assertThat(exception.getMessage()).contains("already exists");
//     }

//     @Test
//     void testUpdateCostFactor_Success() {
//         CostFactor existing = mockCostFactor(FACTOR_ID, "Old Name", COMPANY_ID);
//         CostFactorDto dto = buildCostFactorDto("New Name");

//         when(costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(eq(FACTOR_ID), eq(COMPANY_ID), eq(0)))
//                 .thenReturn(Optional.of(existing));
//         when(costFactorRepository.existsByCompany_CompanyIdAndFactorNameAndDeleteFlag(eq(COMPANY_ID), eq("New Name"), eq(0)))
//                 .thenReturn(false);

//         CostFactorDto updated = costFactorService.updateCostFactor(FACTOR_ID, dto, COMPANY_ID);

//         assertThat(updated.getName()).isEqualTo("New Name");
//         verify(costFactorRepository).save(existing);
//     }

//     @Test
//     void testUpdateCostFactor_DuplicateName() {
//         CostFactor existing = mockCostFactor(FACTOR_ID, "Old Name", COMPANY_ID);
//         CostFactorDto dto = buildCostFactorDto("Duplicate Name");

//         when(costFactorRepository.findByFactorIdAndCompany_CompanyIdAndDeleteFlag(eq(FACTOR_ID), eq(COMPANY_ID), eq(0)))
//                 .thenReturn(Optional.of(existing));
//         when(costFactorRepository.existsByCompany_CompanyIdAndFactorNameAndDeleteFlag(eq(COMPANY_ID), eq("Duplicate Name"), eq(0)))
//                 .thenReturn(true);

//         AppException exception = assertThrows(AppException.class, () -> costFactorService.updateCostFactor(FACTOR_ID, dto, COMPANY_ID));

//         assertThat(exception.getMessage()).contains("already exists");
//     }

//     @Test
//     void testDeleteCostFactor_Success() {
//         CostFactor existing = mockCostFactor(FACTOR_ID, "Existing", COMPANY_ID);
//         existing.setDeleteFlag(0);

//         when(costFactorRepository.findByFactorIdAndCompany_CompanyId(eq(FACTOR_ID), eq(COMPANY_ID)))
//                 .thenReturn(Optional.of(existing));

//         GeneralResponseDto response = costFactorService.deleteCostFactor(FACTOR_ID, COMPANY_ID);

//         assertThat(response.getMessage()).isEqualTo("Cost factor deleted successfully.");
//         assertThat(response.getStatus()).isEqualTo(200);
//         verify(costFactorRepository).save(existing);
//     }

//     @Test
//     void testDeleteCostFactor_AlreadyDeleted() {
//         CostFactor existing = mockCostFactor(FACTOR_ID, "Deleted", COMPANY_ID);
//         existing.setDeleteFlag(1);

//         when(costFactorRepository.findByFactorIdAndCompany_CompanyId(eq(FACTOR_ID), eq(COMPANY_ID)))
//                 .thenReturn(Optional.of(existing));

//         AppException exception = assertThrows(AppException.class, () -> costFactorService.deleteCostFactor(FACTOR_ID, COMPANY_ID));

//         assertThat(exception.getMessage()).isEqualTo(ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST);
//     }

//     @Test
//     void testDeleteCostFactor_NotFound() {
//         when(costFactorRepository.findByFactorIdAndCompany_CompanyId(eq(FACTOR_ID), eq(COMPANY_ID)))
//                 .thenReturn(Optional.empty());

//         AppException exception = assertThrows(AppException.class, () -> costFactorService.deleteCostFactor(FACTOR_ID, COMPANY_ID));

//         assertThat(exception.getMessage()).isEqualTo(ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST);
//     }
// }

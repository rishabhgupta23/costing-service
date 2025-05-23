// package com.services;

// import java.lang.reflect.InvocationTargetException;
// import java.lang.reflect.Method;
// import java.time.ZonedDateTime;

// import com.jubeiwato.costing_service.authentication.config.AppException;

// import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
// import com.jubeiwato.costing_service.dtos.BomDto;
// import com.jubeiwato.costing_service.dtos.PartRequestDto;
// import com.jubeiwato.costing_service.dtos.PartResponseDto;
// import com.jubeiwato.costing_service.dtos.PartUnitDto;
// import com.jubeiwato.costing_service.dtos.VendorCostDto;
// import com.jubeiwato.costing_service.entities.Part;
// import com.jubeiwato.costing_service.entities.PartCost;
// import com.jubeiwato.costing_service.entities.PartCostCostFactor;
// import com.jubeiwato.costing_service.entities.PartUnit;
// import com.jubeiwato.costing_service.entities.Vendor;
// import com.jubeiwato.costing_service.repositories.BomRepository;
// import com.jubeiwato.costing_service.repositories.CategoryRepository;
// import com.jubeiwato.costing_service.repositories.CompanyRepository;
// import com.jubeiwato.costing_service.repositories.CostFactorRepository;
// import com.jubeiwato.costing_service.repositories.PartCostRepository;
// import com.jubeiwato.costing_service.repositories.PartRepository;
// import com.jubeiwato.costing_service.repositories.PartUnitRepository;
// import com.jubeiwato.costing_service.repositories.VendorRepository;
// import com.jubeiwato.costing_service.services.FileGeneratorService;
// import com.jubeiwato.costing_service.services.impl.PartServiceImpl;
// import com.jubeiwato.costing_service.services.impl.VendorServiceImpl;
// import com.jubeiwato.costing_service.dtos.CostFactorDto;
// import com.jubeiwato.costing_service.dtos.CostHistoryDto;
// import com.jubeiwato.costing_service.dtos.CostHistoryResponseDto;
// import com.jubeiwato.costing_service.dtos.FileResponseDto;
// import com.jubeiwato.costing_service.dtos.PartDataDto;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.http.HttpStatus;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyList;
// import static org.mockito.ArgumentMatchers.anySet;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;
// import static org.mockito.ArgumentMatchers.eq;
// import java.io.IOException;
// import org.mockito.*;
// import java.util.*;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.data.domain.Pageable;
// import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
// import com.jubeiwato.costing_service.constants.PartType;
// import com.jubeiwato.costing_service.constants.Sorting;
// import com.jubeiwato.costing_service.dtos.PartDto;
// import com.jubeiwato.costing_service.entities.Bom;
// import com.jubeiwato.costing_service.entities.Company;
// import com.jubeiwato.costing_service.entities.CostFactor;

// @ExtendWith(MockitoExtension.class)
// public class PartServiceTest {

//     @Mock
//     private PartRepository partRepository;

//     @Mock
//     private PartCostRepository partCostRepository;

//     @Mock
//     private PartUnitRepository partUnitRepository;

//     @Mock
//     private BomRepository bomRepository;

//     @InjectMocks
//     private PartServiceImpl partService;

//     @InjectMocks
//     private PartServiceImpl partServiceImpl;

//     @Mock
//     private VendorServiceImpl vendorService;

//     @Mock
//     private CategoryRepository categoryRepository;

//     @Mock
//     private CompanyRepository companyRepository;

//     @InjectMocks
//     private PartServiceTest partServiceTest;

//     @Mock
//     private CostFactorRepository costFactorRepository;
//     @Mock
//     private VendorRepository vendorRepository;

//     @Mock
//     private Part mockPart;

//     @Mock
//     private Part validatedPart;

//     @Mock
//     private Vendor vendor;
//     @Mock
//     private FileGeneratorService excelService;

//     private Long parentPartId = 1L;
//     private Long companyId = 1L;
//     private Part part;
//     private Map<Long, Vendor> vendorMap;

//     private Part parentPart;
//     private Part validChildPart;
//     private PartRequestDto validRequest;
//     private Part validPart;
//     private Vendor validVendor;
//     private CostFactor validCostFactor;

//     @BeforeEach
//     void setUp() {

//         vendorMap = new HashMap<>();

//         vendor = new Vendor();
//         vendor.setVendorId(1L);
//         vendor.setName("Vendor A");
//         vendorMap.put(vendor.getVendorId(), vendor);

//         // Setup mock Part
//         part = new Part();
//         part.setPartId(parentPartId);
//         part.setPartNumber("P1234");
//         Company company = new Company();
//         company.setCompanyId(companyId);
//         part.setCompany(company);
//         part.setUnit("KG");

//         // Setup mock Bom List
//         Bom bom = new Bom();
//         bom.setChildPart(new Part());
//         bom.getChildPart().setPartNumber("C5678");
//         bom.getChildPart().setPartName("Child Part");
//         bom.setQuantity(10.00);

//         company.setCompanyId(companyId);

//         parentPart = new Part();
//         parentPart.setPartId(101L);
//         parentPart.setCompany(company);

//         validChildPart = new Part();
//         validChildPart.setPartId(202L);
//         validChildPart.setPartName("Child Part");
//         validChildPart.setCompany(company);

//         VendorCostDto vendorCost = new VendorCostDto();
//         CostFactorDto costFactorDto = new CostFactorDto();
//         costFactorDto.setId(100L);
//         vendorCost.setCostFactorValues(Collections.singletonList(costFactorDto));

//         validRequest = Mockito.mock(PartRequestDto.class);
//         validRequest.setPartName("Valid Part");
//         validRequest.setPartNumber("12345");
//         validRequest.setType("MASTER");
//         validRequest.setUnit("kg");

//         PartUnit mockPartUnit = new PartUnit();
//         mockPartUnit.setUnitName("kg");

//         validPart = new Part();
//         validPart.setPartName("Valid Part");
//         validPart.setPartNumber("12345");

//         validVendor = new Vendor();
//         validVendor.setVendorId(1L);

//         validCostFactor = new CostFactor();
//         validCostFactor.setFactorId(1L);

//     }

//     @Test
//     void testGetValidatedPart_PartFound() throws Exception {
//         Long partId = 1L;
//         Long companyId = 1L;
//         Part mockPart = Part.builder()
//                 .partId(partId)
//                 .partName("Test Part")
//                 .partNumber("12345")
//                 .categoryName("Category")
//                 .unit("Unit")
//                 .build();

//         // Simulate the part being found
//         when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
//                 .thenReturn(Optional.of(mockPart));

//         // Using reflection to call the private method
//         Method method = PartServiceImpl.class.getDeclaredMethod("getValidatedPart", Long.class, Long.class);
//         method.setAccessible(true);

//         // Invoke the method and assert the result
//         Part result = (Part) method.invoke(partServiceImpl, partId, companyId);

//         assertNotNull(result);
//         assertEquals(partId, result.getPartId());
//         assertEquals("Test Part", result.getPartName());
//         assertEquals("12345", result.getPartNumber());
//     }

//     @Test
//     void testGetValidatedPart_PartNotFound() throws Exception {
//         Long partId = 1L;
//         Long companyId = 1L;

//         when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
//                 .thenReturn(Optional.empty());

//         Method method = PartServiceImpl.class.getDeclaredMethod("getValidatedPart", Long.class, Long.class);
//         method.setAccessible(true);

//         try {
//             method.invoke(partServiceImpl, partId, companyId);
//             fail("Expected AppException to be thrown");
//         } catch (InvocationTargetException ex) {

//             Throwable cause = ex.getCause();
//             assertTrue(cause instanceof AppException,
//                     "Expected AppException but got " + cause.getClass().getSimpleName());

//             AppException appException = (AppException) cause;
//             assertEquals("Part not found", appException.getMessage());
//             assertEquals(HttpStatus.NOT_FOUND, appException.getStatus());
//         }
//     }

//     @Test
//     void validateCreatePartRequest_shouldThrowAppException() throws Exception {
//         Long companyId = 1L;
//         String partNumber = "P12345";

//         PartRequestDto request = PartRequestDto.builder()
//                 .partNumber(partNumber)
//                 .build();

//         when(partRepository.existsByCompany_CompanyIdAndPartNumber(companyId, partNumber)).thenReturn(true);

//         Map<Long, Vendor> vendorMap = new HashMap<>();
//         Map<Long, CostFactor> costFactorMap = new HashMap<>();

//         Method method = PartServiceImpl.class.getDeclaredMethod(
//                 "validateCreatePartRequest",
//                 PartRequestDto.class, Long.class, Map.class, Map.class);
//         method.setAccessible(true);

//         try {
//             method.invoke(partService, request, companyId, vendorMap, costFactorMap);
//             fail("Expected AppException to be thrown");
//         } catch (InvocationTargetException e) {

//             Throwable cause = e.getCause();
//             if (cause instanceof AppException) {
//                 AppException exception = (AppException) cause;
//                 assertEquals(HttpStatus.CONFLICT, exception.getStatus());
//                 assertTrue(exception.getMessage().contains(partNumber));
//             } else {
//                 fail("Unexpected exception type thrown: " + cause);
//             }
//         }
//     }

//     @Test
//     void validateCreatePartRequest_shouldThrowAppException_whenUnitIsNullOrEmpty() throws Exception {
//         Long companyId = 1L;
//         String partNumber = "P12345";

//         PartRequestDto request = PartRequestDto.builder()
//                 .partNumber(partNumber)
//                 .build();

//         Map<Long, Vendor> vendorMap = new HashMap<>();
//         Map<Long, CostFactor> costFactorMap = new HashMap<>();

//         Method method = PartServiceImpl.class.getDeclaredMethod(
//                 "validateCreatePartRequest",
//                 PartRequestDto.class, Long.class, Map.class, Map.class);
//         method.setAccessible(true);

//         try {

//             when(partRepository.existsByCompany_CompanyIdAndPartNumber(companyId, partNumber)).thenReturn(true);

//             // Invoke method
//             method.invoke(partService, request, companyId, vendorMap, costFactorMap);

//             fail("Expected AppException to be thrown");
//         } catch (InvocationTargetException e) {

//             Throwable cause = e.getCause();

//             if (cause instanceof AppException) {
//                 AppException exception = (AppException) cause;

//                 if (exception.getStatus() == HttpStatus.BAD_REQUEST) {
//                     assertTrue(exception.getMessage().contains(ErrorMessageConstant.UNIT_CANNOT_BE_NULL_OR_EMPTY));
//                 }

//             } else {
//                 fail("Unexpected exception type thrown: " + cause);
//             }
//         }

//     }

//     @Test
//     void testGetPartTypes() {
//         List<String> result = partService.getPartTypes();
//         List<String> expected = List.of("MASTER", "UNIT");

//         assertEquals(expected, result);
//     }

//     @Test
//     void testGetPartTypes_ShouldNotReturnNullOrEmpty() {
//         List<String> result = partService.getPartTypes();

//         assertNotNull(result);
//         assertFalse(result.isEmpty());
//     }

//     @Test
//     void testGetPartUnits_PositiveCase() {
//         int pageNo = 0;
//         int pageSize = 2;

//         PartUnit unit1 = new PartUnit(); // mock entity
//         PartUnit unit2 = new PartUnit(); // mock entity

//         List<PartUnit> partUnits = List.of(unit1, unit2);
//         Page<PartUnit> mockPage = new PageImpl<>(partUnits);

//         when(partUnitRepository.findAll(PageRequest.of(pageNo, pageSize))).thenReturn(mockPage);

//         ApiPageResponseDto<List<PartUnitDto>> result = partService.getPartUnits(pageNo, pageSize);

//         assertNotNull(result);
//         assertEquals(2, result.getData().size());
//         assertEquals(pageNo, result.getPageInfo().getPageNumber());
//         assertEquals(pageSize, result.getPageInfo().getPageSize());
//     }

//     @Test
//     void createPartTest_validPart() {
//         Long companyId = 1L;

//         // Create a valid CostFactorDto
//         CostFactorDto costFactorDto = new CostFactorDto();
//         costFactorDto.setId(1L);
//         costFactorDto.setName("Material Cost");
//         costFactorDto.setValue(100.0);

//         VendorCostDto vendorCostDto = VendorCostDto.superBuilder()
//                 .id(1L)
//                 .name("Vendor1")
//                 .emailId("vendor1@example.com")
//                 .address("123 Vendor St")
//                 .contactNumber("1234567890")
//                 .costFactorValues(List.of(costFactorDto))
//                 .build();

//         // Create PartRequestDto
//         PartRequestDto validRequest = PartRequestDto.builder()
//                 .partName("Test Part")
//                 .partNumber("P12345")
//                 .type("MASTER")
//                 .unit("Kg")
//                 .bom(new ArrayList<>()) // Empty BOM
//                 .vendorCostList(Collections.singletonList(vendorCostDto))
//                 .build();

//         Company validCompany = new Company();
//         validCompany.setCompanyId(companyId);
//         validCompany.setCompanyName("Test Company");

//         // Mock CompanyRepository
//         lenient().when(companyRepository.findById(companyId)).thenReturn(Optional.of(validCompany));

//         // Mock PartUnit
//         PartUnit validPartUnit = new PartUnit();
//         validPartUnit.setUnitName("Kg");

//         Vendor validVendor = new Vendor();
//         validVendor.setVendorId(1L);
//         validVendor.setName("Vendor1");
//         validVendor.setEmailId("vendor1@example.com");
//         validVendor.setAddress("123 Vendor St");
//         validVendor.setContactNumber("1234567890");

//         lenient().when(vendorRepository.findByVendorIdInAndCompany_CompanyId(eq(Set.of(1L)), eq(companyId)))
//                 .thenReturn(List.of(validVendor));

//         // Mock CostFactorRepository
//         CostFactor validCostFactor = new CostFactor();
//         validCostFactor.setFactorId(1L);
//         validCostFactor.setFactorName("Material Cost");
//         validCostFactor.setCompany(validCompany);

//         lenient().when(costFactorRepository.findByFactorIdInAndCompany_CompanyId(eq(Set.of(1L)), eq(companyId)))
//                 .thenReturn(List.of(validCostFactor));

//         // Mock other repositories
//         Part validPart = new Part();
//         validPart.setPartName(validRequest.getPartName());
//         validPart.setPartNumber(validRequest.getPartNumber());

//         when(partRepository.existsByCompany_CompanyIdAndPartNumber(companyId, validRequest.getPartNumber()))
//                 .thenReturn(false);
//         when(partRepository.save(any(Part.class))).thenReturn(validPart);
//         when(partCostRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
//         when(partUnitRepository.findByUnitName(anyString())).thenReturn(Optional.of(validPartUnit));

//         // Act: Call the service method
//         partService.createPart(validRequest, companyId);

//         // Assert: Verify the interactions
//         verify(partRepository, times(1)).save(any(Part.class));
//         verify(partCostRepository, times(1)).saveAll(anyList());
//         verify(costFactorRepository, times(1)).findByFactorIdInAndCompany_CompanyId(eq(Set.of(1L)), eq(companyId));
//         verify(companyRepository, times(1)).findById(companyId);
//     }

//     @Test
//     void testValidateAndStoreVendors_ShouldStoreVendorsWhenExist() throws Exception {
//         // Arrange: create mock vendor objects
//         Vendor vendor = new Vendor();
//         vendor.setVendorId(100L);
//         vendor.setName("Vendor A");

//         Set<Long> vendorIds = Set.of(100L); // A valid set of vendor IDs

//         when(vendorRepository.findByVendorIdInAndCompany_CompanyId(vendorIds, companyId))
//                 .thenReturn(List.of(vendor));

//         List<VendorCostDto> vendorCostList = new ArrayList<>();
//         VendorCostDto costDto = new VendorCostDto();
//         costDto.setId(100L);
//         vendorCostList.add(costDto);

//         Map<Long, Vendor> vendorMap = new HashMap<>();

//         Method method = PartServiceImpl.class.getDeclaredMethod(
//                 "validateAndStoreVendors", List.class, Long.class, Map.class);
//         method.setAccessible(true); // Allow access to the private method

//         method.invoke(partService, vendorCostList, companyId, vendorMap);

//         assertEquals(1, vendorMap.size());
//         assertTrue(vendorMap.containsKey(100L)); // The vendor ID should be in the map
//         assertEquals(vendor, vendorMap.get(100L)); // Ensure the vendor object matches
//     }

//     @Test
//     void testValidateAndStoreCostFactors_ShouldPopulateMap_WhenCostFactorsExist() throws Exception {

//         VendorCostDto vendorCost = new VendorCostDto();
//         CostFactorDto costFactorDto = new CostFactorDto();
//         costFactorDto.setId(100L);
//         vendorCost.setCostFactorValues(Collections.singletonList(costFactorDto));

//         List<VendorCostDto> vendorCostList = Collections.singletonList(vendorCost);
//         Long companyId = 1L;
//         Map<Long, CostFactor> costFactorMap = new HashMap<>();

//         CostFactor costFactor = new CostFactor();
//         costFactor.setFactorId(100L);
//         when(costFactorRepository.findByFactorIdInAndCompany_CompanyId(anySet(), eq(companyId)))
//                 .thenReturn(Collections.singletonList(costFactor));

//         Method method = PartServiceImpl.class.getDeclaredMethod("validateAndStoreCostFactors", List.class, Long.class,
//                 Map.class);
//         method.setAccessible(true);

//         method.invoke(partService, vendorCostList, companyId, costFactorMap);

//         assertEquals(1, costFactorMap.size());
//         assertEquals(costFactor, costFactorMap.get(100L));
//     }

//     @Test
//     void testValidateAndSaveBom_WithValidChildren_ShouldSaveBom() throws Exception {

//         BomDto bomDto = new BomDto();
//         bomDto.setChildPartId(validChildPart.getPartId());
//         bomDto.setQuantity(5.0);

//         PartRequestDto requestDto = PartRequestDto.builder()
//                 .partName("Parent Part")
//                 .partNumber("P1234")
//                 .type("Type A")
//                 .unit("kg")
//                 .bom(List.of(bomDto)) // Set BOM list with the created BomDto
//                 .build();

//         when(partRepository.findByPartIdInAndCompany_CompanyId(
//                 Set.of(validChildPart.getPartId()), companyId)).thenReturn(List.of(validChildPart));

//         Method method = PartServiceImpl.class.getDeclaredMethod(
//                 "validateAndSaveBom", PartRequestDto.class, Long.class, Part.class);
//         method.setAccessible(true);

//         method.invoke(partService, requestDto, companyId, parentPart);

//         ArgumentCaptor<List<Bom>> captor = ArgumentCaptor.forClass(List.class);
//         verify(bomRepository).saveAll(captor.capture());

//         // Verify that the BOM list is saved correctly
//         List<Bom> savedBoms = captor.getValue();
//         assertEquals(1, savedBoms.size());
//         Bom savedBom = savedBoms.get(0);

//         assertEquals(validChildPart, savedBom.getChildPart());
//         assertEquals(parentPart, savedBom.getParentPart());
//         assertEquals(5.0, savedBom.getQuantity());
//     }

//     @Test
//     void testCreatePartCostEntity() throws Exception {
//         // Setup the Part entity
//         Part part = new Part();
//         part.setPartId(1L);
//         part.setPartName("Part A");

//         // Setup the VendorCostDto with cost factor values
//         CostFactorDto costFactorDto = new CostFactorDto(1L, "Cost Factor 1", 100.0);
//         VendorCostDto vendorCostDto = new VendorCostDto(
//                 1L, "Vendor A", "123 Vendor St", "vendor@example.com", "123-456-7890",
//                 List.of(costFactorDto));

//         Vendor vendor = new Vendor();
//         vendor.setVendorId(1L);
//         vendor.setName("Vendor A");

//         CostFactor costFactor = new CostFactor();
//         costFactor.setFactorId(1L);
//         costFactor.setFactorName("Cost Factor 1");

//         Map<Long, Vendor> vendorMap = new HashMap<>();
//         vendorMap.put(vendor.getVendorId(), vendor);

//         Map<Long, CostFactor> costFactorMap = new HashMap<>();
//         costFactorMap.put(costFactor.getFactorId(), costFactor);

//         Method createPartCostEntityMethod = PartServiceImpl.class.getDeclaredMethod(
//                 "createPartCostEntity", Part.class, VendorCostDto.class, Map.class, Map.class);
//         createPartCostEntityMethod.setAccessible(true); // Make it accessible

//         PartCost partCost = (PartCost) createPartCostEntityMethod.invoke(
//                 partServiceImpl, part, vendorCostDto, vendorMap, costFactorMap);

//         // Assertions
//         assertNotNull(partCost);
//         assertEquals(1L, partCost.getPart().getPartId());
//         assertEquals("Part A", partCost.getPart().getPartName());
//         assertEquals(1L, partCost.getVendor().getVendorId());
//         assertEquals("Vendor A", partCost.getVendor().getName());

//         // Verify the cost factor list is not null and contains the correct data
//         assertNotNull(partCost.getCostFactorList());
//         assertEquals(1, partCost.getCostFactorList().size());

//         // Test PartCostCostFactor associations
//         PartCostCostFactor partCostCostFactor = partCost.getCostFactorList().get(0);
//         assertNotNull(partCostCostFactor);
//         assertEquals(1L, partCostCostFactor.getCostFactor().getFactorId());
//         assertEquals("Cost Factor 1", partCostCostFactor.getCostFactor().getFactorName());
//         assertEquals(100.0, partCostCostFactor.getValue(), 0.001); // Allow a small delta for floating-point comparisons
//         assertEquals(partCost, partCostCostFactor.getPartCost());
//     }

//     @Test
//     void updatePartById_shouldUpdatePartSuccessfully() {
//         // Setup mock data
//         Long partId = 1L;
//         Long companyId = 1L;

//         // Prepare mock PartRequestDto with vendor cost list and BOM
//         List<BomDto> bomList = new ArrayList<>();
//         BomDto bomDto = BomDto.builder()
//                 .childPartId(2L) // Mock valid child part ID
//                 .quantity(10.00)
//                 .build();
//         bomList.add(bomDto);

//         PartRequestDto partRequestDto = PartRequestDto.builder()
//                 .partName("Updated Part")
//                 .partNumber("P12345")
//                 .type("UNIT")
//                 .unit("kg")
//                 .vendorCostList(new ArrayList<>()) // Mock an empty list (or add mock data)
//                 .bom(bomList) // Ensure BOM list is provided
//                 .build();

//         // Mock the existing part that should be returned by getValidatedPart
//         Part existingPart = new Part();
//         existingPart.setPartId(partId);
//         existingPart.setPartNumber("P12345");
//         existingPart.setPartName("Old Part");

//         Part childPart = new Part();
//         childPart.setPartId(2L);
//         when(partRepository.findById(2L)).thenReturn(Optional.of(childPart));

//         when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId)).thenReturn(Optional.of(existingPart));
//         when(partUnitRepository.findByUnitName(partRequestDto.getUnit())).thenReturn(Optional.of(new PartUnit()));

//         Bom newBom = new Bom(existingPart, childPart, bomDto.getQuantity());
//         when(bomRepository.saveAll(anyList())).thenReturn(Collections.singletonList(newBom));

//         // Call the service method
//         PartDto updatedPartDto = partService.updatePartById(partId, partRequestDto, companyId);

//         // Verify the interactions
//         verify(partRepository, times(1)).save(existingPart); // Ensure the part was saved
//         verify(partCostRepository, times(1)).saveAll(anyList()); // Ensure PartCost was saved
//         verify(bomRepository, times(1)).deleteByParentPart(existingPart); // Ensure BOM was deleted
//         verify(bomRepository, times(1)).saveAll(anyList()); // Ensure BOM was saved

//         // Assert the result (you can add more specific assertions based on expected
//         // behavior)
//         assertNotNull(updatedPartDto);
//         assertEquals("Updated Part", updatedPartDto.getPartName());
//     }

//     @Test
//     void testGetCostCostFactors() {
//         int pageNo = 0;
//         int pageSize = 2;
//         Long companyId = 123L;

//         // Create a mock CostFactor object
//         CostFactor costFactor = new CostFactor();
//         costFactor.setFactorId(1L); // Assuming the getter/setter for factorId exists
//         costFactor.setFactorName("CostFactor 1");

//         // Create a Page of CostFactors, which is what the repository returns
//         Page<CostFactor> costFactorPage = new PageImpl<>(List.of(costFactor));

//         // Mock the repository's response
//         when(costFactorRepository.findByCompany_CompanyId(companyId, PageRequest.of(pageNo, pageSize)))
//                 .thenReturn(costFactorPage);

//         // Call the method you're testing
//         ApiPageResponseDto<List<CostFactorDto>> response = partService.getCostFactors(pageNo, pageSize, companyId);

//         // Assert the results
//         assertNotNull(response);
//         assertEquals(1, response.getData().size());
//         assertEquals("CostFactor 1", response.getData().get(0).getName());
//         assertEquals(1, response.getPageInfo().getTotalPages());
//         assertEquals(2, response.getPageInfo().getPageSize());
//         assertEquals(1L, response.getPageInfo().getTotalRecords());
//     }

//     @Test
//     void testGetCostFactors() {
//         int pageNo = 0;
//         int pageSize = 2;
//         Long companyId = 123L;

//         CostFactor costFactor = new CostFactor();
//         costFactor.setFactorId(1L); // Make sure the getter is getFactorId()
//         costFactor.setFactorName("CostFactor 1");

//         Double value = 100.0;

//         Page<CostFactor> costFactorPage = new PageImpl<>(List.of(costFactor));

//         when(costFactorRepository.findByCompany_CompanyId(companyId, PageRequest.of(pageNo, pageSize)))
//                 .thenReturn(costFactorPage);

//         ApiPageResponseDto<List<CostFactorDto>> response = partService.getCostFactors(pageNo, pageSize, companyId);

//         assertNotNull(response);
//         assertEquals(1, response.getData().size());
//         assertEquals("CostFactor 1", response.getData().get(0).getName());
//         assertEquals(1, response.getPageInfo().getTotalPages());
//         assertEquals(2, response.getPageInfo().getPageSize());
//         assertEquals(1L, response.getPageInfo().getTotalRecords());
//     }

//     @Test
//     void testGetPartsBasic() {
//         int pageNo = 0;
//         int pageSize = 2;
//         String sortBy = "partId";
//         Sorting sortMode = Sorting.ASC;
//         long companyId = 123L;
//         Part filter = new Part();

//         // Sample Vendor
//         Vendor vendor = new Vendor();
//         vendor.setName("Vendor1");

//         // Sample PartCost
//         PartCost partCost = new PartCost();
//         partCost.setVendor(vendor);

//         // Sample Part
//         Part part = new Part();
//         part.setPartId(1L);
//         part.setPartName("Gear");
//         part.setPartNumber("G123");
//         part.setCategoryName("Mechanical");
//         part.setType(PartType.UNIT); // use enum
//         part.setUnit("pcs");
//         part.setPartCosts(new ArrayList<>(Set.of(partCost)));

//         List<Part> partList = List.of(part);
//         Page<Part> partPage = new PageImpl<>(partList, PageRequest.of(pageNo, pageSize), 1);

//         when(partRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(partPage);
//         when(partCostRepository.getMaxVendorCount()).thenReturn(3);

//         ApiPageResponseDto<PartDataDto> response = partService.getParts(filter, companyId, pageNo, pageSize, sortBy,
//                 sortMode);

//         assertNotNull(response);
//         assertEquals(1, response.getData().getPartsList().size());
//         assertEquals("Gear", response.getData().getPartsList().get(0).getPartName());
//         assertEquals(3, response.getData().getMaxVendorCount());
//         assertEquals(1, response.getPageInfo().getTotalPages());

//         verify(partRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
//     }

//     @Test
//     void testValidateCreatePartRequest_PartNumberExists() throws Exception {
//         // Arrange
//         PartRequestDto request = PartRequestDto.builder() // Using Lombok's builder
//                 .partName("Part A")
//                 .partNumber("P1234")
//                 .categoryId(1L)
//                 .type("Type A")
//                 .unit("kg")
//                 .vendorCostList(new ArrayList<>())
//                 .bom(new ArrayList<>())
//                 .build();
//         Long companyId = 1L;
//         Map<Long, Vendor> vendorMap = new HashMap<>(); // Setup vendor map
//         Map<Long, CostFactor> costFactorMap = new HashMap<>(); // Setup cost factor map

//         // Use reflection to invoke the private method
//         Method method = PartServiceImpl.class.getDeclaredMethod("validateCreatePartRequest", PartRequestDto.class,
//                 Long.class, Map.class, Map.class);
//         method.setAccessible(true);

//         // Act & Assert
//         try {
//             method.invoke(partService, request, companyId, vendorMap, costFactorMap);
//             fail("Expected AppException was not thrown");
//         } catch (InvocationTargetException e) {
//             // Unwrap the exception
//             Throwable cause = e.getCause();
//             assertTrue(cause instanceof AppException);
//             assertEquals("Invalid Category", cause.getMessage());
//         }

//     }

//     @Test
//     void testGetPartById() {
//         Long partId = 1L;
//         Long companyId = 100L;

//         when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
//                 .thenReturn(Optional.of(validatedPart));

//         when(partCostRepository.findByPartId(partId)).thenReturn(List.of());

//         when(bomRepository.findByParentPart(validatedPart)).thenReturn(List.of());

//         PartDto result = partService.getPartById(partId, companyId);

//         assertNotNull(result, "The result should not be null");
//     }

//     @Test
//     void testGetPartById_PartNotFound() {
//         Long partId = 1L;
//         Long companyId = 100L;

//         when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
//                 .thenReturn(Optional.empty());

//         AppException exception = assertThrows(AppException.class, () -> {
//             partService.getPartById(partId, companyId);
//         });

//         assertEquals(ErrorMessageConstant.PART_NOT_FOUND, exception.getMessage());
//         assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
//     }

//     @Test
//     void testCreatePartResponseDto() throws Exception {

//         Part part = new Part();
//         part.setPartId(1L);
//         part.setPartName("Screw");
//         // Other properties

//         List<PartCost> partCostList = new ArrayList<>();
//         List<Bom> bomList = new ArrayList<>();

//         Method method = PartServiceImpl.class.getDeclaredMethod("createPartResponseDto", Part.class, List.class,
//                 List.class);
//         method.setAccessible(true);

//         PartResponseDto dto = (PartResponseDto) method.invoke(partServiceImpl, part, partCostList, bomList);

//         assertNotNull(dto);
//         assertEquals(1L, dto.getPartId());
//         assertEquals("Screw", dto.getPartName());
//     }

//     @Test
//     void testDownloadPartsToExcel() throws IOException {
//         Long companyId = 1L;

//         Company company = Company.builder()
//                 .companyId(companyId)
//                 .build();

//         Part part1 = Part.builder()
//                 .partNumber("P001")
//                 .partName("Part One")
//                 .unit("kg")
//                 .type(PartType.UNIT)
//                 .categoryName("Category1")
//                 .company(company)
//                 .partCosts(Collections.emptyList())
//                 .build();

//         Part part2 = Part.builder()
//                 .partNumber("P002")
//                 .partName("Part Two")
//                 .unit("m")
//                 .type(PartType.UNIT)
//                 .categoryName("Category2")
//                 .company(company)
//                 .partCosts(Collections.emptyList())
//                 .build();

//         when(partRepository.findByCompany_CompanyId(eq(companyId), any()))
//                 .thenReturn(Arrays.asList(part1, part2));

//         byte[] mockExcelData = new byte[1]; // Simulated byte data
//         when(excelService.generateSpreadsheet(any(), any())).thenReturn(mockExcelData);

//         byte[] result = partService.downloadPartsToExcel(companyId);

//         assertNotNull(result, "The generated Excel data should not be null.");
//         assertArrayEquals(mockExcelData, result, "The generated Excel data should match the expected byte array.");
//     }

//     @Test
//     void testCreateVendorCostList() {
//         // Mock Vendor
//         Vendor vendor = new Vendor();
//         vendor.setVendorId(1L);
//         vendor.setName("Vendor 1");
//         vendor.setAddress("Vendor Address");
//         vendor.setEmailId("vendor1@example.com");
//         vendor.setContactNumber("1234567890");

//         CostFactor costFactor = new CostFactor();
//         costFactor.setFactorId(1L);
//         costFactor.setFactorName("Factor A");

//         PartCostCostFactor costFactorLink = new PartCostCostFactor();
//         costFactorLink.setCostFactor(costFactor);
//         costFactorLink.setValue(100.0);

//         PartCost partCost = new PartCost();
//         partCost.setVendor(vendor);
//         partCost.setCostFactorList(Arrays.asList(costFactorLink));

//         List<PartCost> partCostList = Arrays.asList(partCost);

//         List<VendorCostDto> vendorCostList = partService.createVendorCostList(partCostList);

//         assertNotNull(vendorCostList);
//         assertFalse(vendorCostList.isEmpty());

//         VendorCostDto vendorCostDto = vendorCostList.get(0);
//         assertEquals(1L, vendorCostDto.getId());
//         assertEquals("Vendor 1", vendorCostDto.getName());
//         assertEquals("Vendor Address", vendorCostDto.getAddress());
//         assertEquals("vendor1@example.com", vendorCostDto.getEmailId());
//         assertEquals("1234567890", vendorCostDto.getContactNumber());

//         assertNotNull(vendorCostDto.getCostFactorValues());
//         assertEquals(1, vendorCostDto.getCostFactorValues().size());
//         CostFactorDto costFactorDto = vendorCostDto.getCostFactorValues().get(0);
//         assertEquals(1L, costFactorDto.getId());
//         assertEquals("Factor A", costFactorDto.getName());
//         assertEquals(100.0, costFactorDto.getValue()); // Ensure the value is correctly set
//     }

//     @Test
//     void testCreateVendorCostList_EmptyList() {

//         List<PartCost> partCostList = new ArrayList<>();

//         List<VendorCostDto> result = partService.createVendorCostList(partCostList);
//         assertTrue(result.isEmpty(), "The list should be empty when no PartCosts are provided.");
//     }

//     @Test
//     void testDeletePartById_Valid() {
//         Long partId = 1L;
//         Long companyId = 2L;

//         Part part = mock(Part.class);
//         when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
//                 .thenReturn(Optional.of(part));
//         when(bomRepository.existsByChildPart(part)).thenReturn(false);

//         partServiceImpl.deletePartById(partId, companyId);

//         verify(partRepository).delete(part);
//     }

//     @Test
//     void testDeletePartById_WhenPartIsChild_ThrowsException() {
//         Long partId = 1L;
//         Long companyId = 2L;

//         Part part = mock(Part.class);
//         when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
//                 .thenReturn(Optional.of(part));
//         when(bomRepository.existsByChildPart(part)).thenReturn(true);

//         AppException exception = assertThrows(AppException.class, () -> {
//             partServiceImpl.deletePartById(partId, companyId);
//         });

//         assertEquals(ErrorMessageConstant.RESTRICT_CHILD_PART_DELETE, exception.getMessage());
//         assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
//         partRepository.delete((Part) part);

//     }

//     @Test
//     void testGetPartCostsByPartAndVendor_Valid() {
//         Long partId = 1L;
//         Long vendorId = 2L;
//         Long companyId = 3L;

//         Part part = mock(Part.class);
//         when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
//                 .thenReturn(Optional.of(part));

//         CostFactor costFactor = mock(CostFactor.class);
//         when(costFactor.getFactorId()).thenReturn(10L);
//         when(costFactor.getFactorName()).thenReturn("Material");

//         PartCostCostFactor costFactorLink = mock(PartCostCostFactor.class);
//         when(costFactorLink.getCostFactor()).thenReturn(costFactor);
//         when(costFactorLink.getValue()).thenReturn(100.0);

//         PartCost partCost = mock(PartCost.class);
//         when(partCost.getUpdatedDateTime()).thenReturn(ZonedDateTime.now());
//         when(partCost.getCostFactorList()).thenReturn(List.of(costFactorLink));

//         List<PartCost> partCosts = List.of(partCost);
//         when(partCostRepository.fetchByPartIdAndVendorId(partId, vendorId)).thenReturn(partCosts);

//         CostHistoryResponseDto result = partServiceImpl.getPartCostsByPartAndVendor(partId, vendorId, companyId);

//         assertNotNull(result);
//         assertEquals(partId, result.getPartId());
//         assertEquals(vendorId, result.getVendorId());
//         assertEquals(1, result.getCostHistoryList().size());

//         CostHistoryDto history = result.getCostHistoryList().get(0);
//         assertEquals(1, history.getCostFactorList().size());

//         CostFactorDto factorDto = history.getCostFactorList().get(0);
//         assertEquals(10L, factorDto.getId());
//         assertEquals("Material", factorDto.getName());
//         assertEquals(100.0, factorDto.getValue());
//     }

//     @Test
//     void testGetPartUnits_EmptyPage_ShouldReturnEmptyList() {
//         int pageNo = 0;
//         int pageSize = 5;

//         Page<PartUnit> emptyPage = new PageImpl<>(Collections.emptyList());

//         when(partUnitRepository.findAll(PageRequest.of(pageNo, pageSize))).thenReturn(emptyPage);

//         ApiPageResponseDto<List<PartUnitDto>> result = partService.getPartUnits(pageNo, pageSize);

//         assertNotNull(result);
//         assertTrue(result.getData().isEmpty());
//         assertEquals(0, result.getPageInfo().getTotalRecords());
//     }

//     @Test
//     void testDownloadBomPartListToExcel_EmptyBomList() throws IOException {
//         // Arrange
//         when(partRepository.findById(parentPartId)).thenReturn(Optional.of(part));
//         when(bomRepository.findByParentPart_PartId(parentPartId)).thenReturn(Collections.emptyList());
//         byte[] mockExcelData = new byte[] { 1, 2, 3 }; // Mock empty Excel data
//         when(excelService.generateSpreadsheet(any(), any())).thenReturn(mockExcelData);

//         // Act
//         FileResponseDto response = partServiceImpl.downloadBomPartListToExcel(parentPartId, companyId);

//         // Assert
//         assertNotNull(response);
//         assertEquals("P1234_Bo", response.getFileName().substring(0, 8));
//     }

//     @Test
//     void testDownloadBomPartListToExcel_WithBomData() throws IOException {
//         Long parentPartId = 1L;
//         Long companyId = 100L;

//         Company company = new Company();
//         company.setCompanyId(companyId);

//         Part parentPart = new Part();
//         parentPart.setPartId(parentPartId);
//         parentPart.setPartNumber("PARENT456");
//         parentPart.setCompany(company);

//         Part childPart = new Part();
//         childPart.setPartId(2L);
//         childPart.setPartNumber("CHILD1");
//         childPart.setPartName("Child Part 1");

//         Bom bom = new Bom();
//         bom.setChildPart(childPart);
//         bom.setQuantity(5.00);

//         List<Bom> bomList = List.of(bom);

//         when(partRepository.findById(parentPartId)).thenReturn(Optional.of(parentPart));
//         when(bomRepository.findByParentPart_PartId(parentPartId)).thenReturn(bomList);
//         when(excelService.generateSpreadsheet(anyList(), any())).thenReturn(new byte[] { 9, 9, 9 });

//         FileResponseDto response = partService.downloadBomPartListToExcel(parentPartId, companyId);

//         assertNotNull(response);
//         assertTrue(response.getFileName().contains("PARENT456_Bom_"));
//         assertFalse(response.getFileData().isEmpty());
//     }
// }

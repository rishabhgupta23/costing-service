package com.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;

import com.jubeiwato.costing_service.authentication.config.AppException;

import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.AttributeValueDto;
import com.jubeiwato.costing_service.dtos.BomDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.dtos.PartUnitDto;
import com.jubeiwato.costing_service.dtos.VendorCostDto;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartAttribute;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import com.jubeiwato.costing_service.entities.PartFile;
import com.jubeiwato.costing_service.entities.PartPartAttribute;
import com.jubeiwato.costing_service.entities.PartUnit;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.CategoryRepository;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.CostFactorRepository;
import com.jubeiwato.costing_service.repositories.PartAttributeRepository;
import com.jubeiwato.costing_service.repositories.PartCostRepository;
import com.jubeiwato.costing_service.repositories.PartFileRepository;
import com.jubeiwato.costing_service.repositories.PartPartAttributeRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.repositories.PartUnitRepository;
import com.jubeiwato.costing_service.repositories.VendorRepository;
import com.jubeiwato.costing_service.services.FileGeneratorService;
import com.jubeiwato.costing_service.services.S3Service;
import com.jubeiwato.costing_service.services.impl.PartServiceImpl;
import com.jubeiwato.costing_service.services.impl.S3ServiceImpl;
import com.jubeiwato.costing_service.services.impl.VendorServiceImpl;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.CostHistoryDto;
import com.jubeiwato.costing_service.dtos.CostHistoryResponseDto;
import com.jubeiwato.costing_service.dtos.FileResponseDto;
import com.jubeiwato.costing_service.dtos.PartDataDto;
import com.jubeiwato.costing_service.dtos.PartResponseDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import java.io.IOException;
import org.mockito.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartFileUploadDto;
import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.Category;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.CostFactor;

@ExtendWith(MockitoExtension.class)
public class PartServiceTest {

    @Mock
    private PartRepository partRepository;

    @Mock
    private PartCostRepository partCostRepository;

    @Mock
    private PartUnitRepository partUnitRepository;

    @Mock
    private BomRepository bomRepository;

    @InjectMocks
    private PartServiceImpl partServiceImpl;

    @Mock
    private VendorServiceImpl vendorService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CostFactorRepository costFactorRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private Part mockPart;

    @Mock
    private Part validatedPart;

    @Mock
    private PartFileRepository partFileRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private FileGeneratorService excelService;
    
    @Mock
    private PartAttributeRepository partAttributeRepository;

    @Mock
    private PartPartAttributeRepository partPartAttributeRepository;


    @InjectMocks
    private S3ServiceImpl s3ServiceImpl;

    private Long parentPartId = 1L;
    private Long companyId = 1L;
    private Part part;
    private Map<Long, Vendor> vendorMap;

    private Part parentPart;
    private Part validChildPart;
    private PartRequestDto validRequest;
    private Part validPart;
    private Vendor validVendor;
    private CostFactor validCostFactor;
    private Vendor vendor;

    @BeforeEach
    void setUp() {

        vendorMap = new HashMap<>();

        vendor = new Vendor();
        vendor.setVendorId(1L);
        vendor.setVendorName("Vendor A");
        vendorMap.put(vendor.getVendorId(), vendor);

        // Setup mock Part
        part = new Part();
        part.setPartId(parentPartId);
        part.setPartNumber("P1234");
        Company company = new Company();
        company.setCompanyId(companyId);
        part.setCompany(company);
        part.setUnit("KG");

        // Setup mock Bom List
        Bom bom = new Bom();
        bom.setChildPart(new Part());
        bom.getChildPart().setPartNumber("C5678");
        bom.getChildPart().setPartName("Child Part");
        bom.setQuantity(10.00);

        company.setCompanyId(companyId);

        parentPart = new Part();
        parentPart.setPartId(101L);
        parentPart.setCompany(company);

        validChildPart = new Part();
        validChildPart.setPartId(202L);
        validChildPart.setPartName("Child Part");
        validChildPart.setCompany(company);

        VendorCostDto vendorCost = new VendorCostDto();
        CostFactorDto costFactorDto = new CostFactorDto();
        costFactorDto.setId(100L);
        vendorCost.setCostFactorValues(Collections.singletonList(costFactorDto));

        validRequest = Mockito.mock(PartRequestDto.class);
        validRequest.setPartName("Valid Part");
        validRequest.setPartNumber("12345");
        validRequest.setType("MASTER");
        validRequest.setUnit("kg");

        PartUnit mockPartUnit = new PartUnit();
        mockPartUnit.setUnitName("kg");

        validPart = new Part();
        validPart.setPartName("Valid Part");
        validPart.setPartNumber("12345");

        validVendor = new Vendor();
        validVendor.setVendorId(1L);

        validCostFactor = new CostFactor();
        validCostFactor.setFactorId(1L);

    }

    @Test
    void testGetValidatedPart_PartFound() throws Exception {
        Long partId = 1L;
        Long companyId = 1L;

        Category category = Category.builder()
                .categoryId(1L)
                .build();

        Part mockPart = Part.builder()
                .partId(partId)
                .partName("Test Part")
                .partNumber("12345")
                .category(category)
                .unit("Unit")
                .type(PartType.MASTER)
                .build();

        // Simulate the part being found
        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.of(mockPart));

        // Using reflection to call the private method
        Method method = PartServiceImpl.class.getDeclaredMethod("getValidatedPart", Long.class, Long.class);
        method.setAccessible(true);

        // Invoke the method and assert the result
        Part result = (Part) method.invoke(partServiceImpl, partId, companyId);

        assertNotNull(result);
        assertEquals(partId, result.getPartId());
        assertEquals("Test Part", result.getPartName());
        assertEquals("12345", result.getPartNumber());
    }

    @Test
    void testGetValidatedPart_PartNotFound() throws Exception {
        Long partId = 1L;
        Long companyId = 1L;

        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.empty());

        Method method = PartServiceImpl.class.getDeclaredMethod("getValidatedPart", Long.class, Long.class);
        method.setAccessible(true);

        try {
            method.invoke(partServiceImpl, partId, companyId);
            fail("Expected AppException to be thrown");
        } catch (InvocationTargetException ex) {

            Throwable cause = ex.getCause();
            assertTrue(cause instanceof AppException,
                    "Expected AppException but got " + cause.getClass().getSimpleName());

            AppException appException = (AppException) cause;
            assertEquals("Part not found", appException.getMessage());
            assertEquals(HttpStatus.NOT_FOUND, appException.getStatus());
        }
    }

    @Test
    void validateCreatePartRequest_shouldThrowAppException() throws Exception {
        Long companyId = 1L;
        String partNumber = "P12345";

        PartRequestDto request = PartRequestDto.builder()
                .partNumber(partNumber)
                .build();

        when(partRepository.existsByCompany_CompanyIdAndPartNumber(companyId, partNumber)).thenReturn(true);

        Map<Long, Vendor> vendorMap = new HashMap<>();
        Map<Long, CostFactor> costFactorMap = new HashMap<>();

        Method method = PartServiceImpl.class.getDeclaredMethod(
                "validateCreatePartRequest",
                PartRequestDto.class, Long.class, Map.class, Map.class);
        method.setAccessible(true);

        try {
            method.invoke(partServiceImpl, request, companyId, vendorMap, costFactorMap);
            fail("Expected AppException to be thrown");
        } catch (InvocationTargetException e) {

            Throwable cause = e.getCause();
            if (cause instanceof AppException) {
                AppException exception = (AppException) cause;
                assertEquals(HttpStatus.CONFLICT, exception.getStatus());
                assertTrue(exception.getMessage().contains(partNumber));
            } else {
                fail("Unexpected exception type thrown: " + cause);
            }
        }
    }

    @Test
    void validateCreatePartRequest_shouldThrowAppException_whenUnitIsNullOrEmpty() throws Exception {
        Long companyId = 1L;
        String partNumber = "P12345";

        PartRequestDto request = PartRequestDto.builder()
                .partNumber(partNumber)
                .build();

        Map<Long, Vendor> vendorMap = new HashMap<>();
        Map<Long, CostFactor> costFactorMap = new HashMap<>();

        Method method = PartServiceImpl.class.getDeclaredMethod(
                "validateCreatePartRequest",
                PartRequestDto.class, Long.class, Map.class, Map.class);
        method.setAccessible(true);

        try {

            when(partRepository.existsByCompany_CompanyIdAndPartNumber(companyId, partNumber)).thenReturn(true);

            // Invoke method
            method.invoke(partServiceImpl, request, companyId, vendorMap, costFactorMap);

            fail("Expected AppException to be thrown");
        } catch (InvocationTargetException e) {

            Throwable cause = e.getCause();

            if (cause instanceof AppException) {
                AppException exception = (AppException) cause;

                if (exception.getStatus() == HttpStatus.BAD_REQUEST) {
                    assertTrue(exception.getMessage().contains(ErrorMessageConstant.UNIT_CANNOT_BE_NULL_OR_EMPTY));
                }

            } else {
                fail("Unexpected exception type thrown: " + cause);
            }
        }

    }

    @Test
    void testGetPartTypes() {
        List<String> result = partServiceImpl.getPartTypes();
        List<String> expected = List.of("MASTER", "UNIT");

        assertEquals(expected, result);
    }

    @Test
    void testGetPartTypes_ShouldNotReturnNullOrEmpty() {
        List<String> result = partServiceImpl.getPartTypes();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetPartUnits_PositiveCase() {
        int pageNo = 0;
        int pageSize = 2;

        PartUnit unit1 = new PartUnit(); // mock entity
        PartUnit unit2 = new PartUnit(); // mock entity

        List<PartUnit> partUnits = List.of(unit1, unit2);
        Page<PartUnit> mockPage = new PageImpl<>(partUnits);

        when(partUnitRepository.findAll(PageRequest.of(pageNo, pageSize))).thenReturn(mockPage);

        ApiPageResponseDto<List<PartUnitDto>> result = partServiceImpl.getPartUnits(pageNo, pageSize);

        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(pageNo, result.getPageInfo().getPageNumber());
        assertEquals(pageSize, result.getPageInfo().getPageSize());
    }

    @Test
    void createPartTest_validPart() {
        Long companyId = 1L;

        // Create a valid CostFactorDto
        CostFactorDto costFactorDto = new CostFactorDto();
        costFactorDto.setId(1L);
        costFactorDto.setFactorName("Material Cost");
        costFactorDto.setValue(100.0);

        VendorCostDto vendorCostDto = VendorCostDto.superBuilder()
                .id(1L)
                .vendorName("Vendor1")
                .emailId("vendor1@example.com")
                .address("123 Vendor St")
                .contactNumber("1234567890")
                .costFactorValues(List.of(costFactorDto))
                .build();

        // Create PartRequestDto
        PartRequestDto validRequest = PartRequestDto.builder()
                .partName("Test Part")
                .partNumber("P12345")
                .type("MASTER")
                .unit("Kg")
                .bom(new ArrayList<>()) // Empty BOM
                .vendorCostList(Collections.singletonList(vendorCostDto))
                .build();

        Company validCompany = new Company();
        validCompany.setCompanyId(companyId);
        validCompany.setCompanyName("Test Company");

        // Mock CompanyRepository
        lenient().when(companyRepository.findById(companyId)).thenReturn(Optional.of(validCompany));

        // Mock PartUnit
        PartUnit validPartUnit = new PartUnit();
        validPartUnit.setUnitName("Kg");

        Vendor validVendor = new Vendor();
        validVendor.setVendorId(1L);
        validVendor.setVendorName("Vendor1");
        validVendor.setEmailId("vendor1@example.com");
        validVendor.setAddress("123 Vendor St");
        validVendor.setContactNumber("1234567890");

        lenient().when(vendorRepository.findByVendorIdInAndCompany_CompanyId(eq(Set.of(1L)), eq(companyId)))
                .thenReturn(List.of(validVendor));

        // Mock CostFactorRepository
        CostFactor validCostFactor = new CostFactor();
        validCostFactor.setFactorId(1L);
        validCostFactor.setFactorName("Material Cost");
        validCostFactor.setCompany(validCompany);

        lenient().when(costFactorRepository.findByFactorIdInAndCompany_CompanyId(eq(Set.of(1L)), eq(companyId)))
                .thenReturn(List.of(validCostFactor));

        // Mock other repositories
        Part validPart = new Part();
        validPart.setPartName(validRequest.getPartName());
        validPart.setPartNumber(validRequest.getPartNumber());

        when(partRepository.existsByCompany_CompanyIdAndPartNumber(companyId, validRequest.getPartNumber()))
                .thenReturn(false);
        when(partRepository.save(any(Part.class))).thenReturn(validPart);
        when(partCostRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(partUnitRepository.findByUnitName(anyString())).thenReturn(Optional.of(validPartUnit));

        // Act: Call the service method
        partServiceImpl.createPart(validRequest, companyId);

        // Assert: Verify the interactions
        verify(partRepository, times(1)).save(any(Part.class));
        verify(partCostRepository, times(1)).saveAll(anyList());
        verify(costFactorRepository, times(1)).findByFactorIdInAndCompany_CompanyId(eq(Set.of(1L)), eq(companyId));
        verify(companyRepository, times(1)).findById(companyId);
    }

    @Test
    void testValidateAndStoreVendors_ShouldStoreVendorsWhenExist() throws Exception {
        // Arrange: create mock vendor objects
        Vendor vendor = new Vendor();
        vendor.setVendorId(100L);
        vendor.setVendorName("Vendor A");

        Set<Long> vendorIds = Set.of(100L); // A valid set of vendor IDs

        when(vendorRepository.findByVendorIdInAndCompany_CompanyId(vendorIds, companyId))
                .thenReturn(List.of(vendor));

        List<VendorCostDto> vendorCostList = new ArrayList<>();
        VendorCostDto costDto = new VendorCostDto();
        costDto.setId(100L);
        vendorCostList.add(costDto);

        Map<Long, Vendor> vendorMap = new HashMap<>();

        Method method = PartServiceImpl.class.getDeclaredMethod(
                "validateAndStoreVendors", List.class, Long.class, Map.class);
        method.setAccessible(true); // Allow access to the private method

        method.invoke(partServiceImpl, vendorCostList, companyId, vendorMap);

        assertEquals(1, vendorMap.size());
        assertTrue(vendorMap.containsKey(100L)); // The vendor ID should be in the map
        assertEquals(vendor, vendorMap.get(100L)); // Ensure the vendor object matches
    }

    @Test
    void testValidateAndStoreCostFactors_ShouldPopulateMap_WhenCostFactorsExist() throws Exception {

        VendorCostDto vendorCost = new VendorCostDto();
        CostFactorDto costFactorDto = new CostFactorDto();
        costFactorDto.setId(100L);
        vendorCost.setCostFactorValues(Collections.singletonList(costFactorDto));

        List<VendorCostDto> vendorCostList = Collections.singletonList(vendorCost);
        Long companyId = 1L;
        Map<Long, CostFactor> costFactorMap = new HashMap<>();

        CostFactor costFactor = new CostFactor();
        costFactor.setFactorId(100L);
        when(costFactorRepository.findByFactorIdInAndCompany_CompanyId(anySet(), eq(companyId)))
                .thenReturn(Collections.singletonList(costFactor));

        Method method = PartServiceImpl.class.getDeclaredMethod("validateAndStoreCostFactors", List.class, Long.class,
                Map.class);
        method.setAccessible(true);

        method.invoke(partServiceImpl, vendorCostList, companyId, costFactorMap);

        assertEquals(1, costFactorMap.size());
        assertEquals(costFactor, costFactorMap.get(100L));
    }

    @Test
    void testValidateAndSaveBom_WithValidChildren_ShouldSaveBom() throws Exception {

        BomDto bomDto = new BomDto();
        bomDto.setChildPartId(validChildPart.getPartId());
        bomDto.setQuantity(5.0);

        PartRequestDto requestDto = PartRequestDto.builder()
                .partName("Parent Part")
                .partNumber("P1234")
                .type("Type A")
                .unit("kg")
                .bom(List.of(bomDto)) // Set BOM list with the created BomDto
                .build();

        when(partRepository.findByPartIdInAndCompany_CompanyId(
                Set.of(validChildPart.getPartId()), companyId)).thenReturn(List.of(validChildPart));

        Method method = PartServiceImpl.class.getDeclaredMethod(
                "validateAndSaveBom", PartRequestDto.class, Long.class, Part.class);
        method.setAccessible(true);

        method.invoke(partServiceImpl, requestDto, companyId, parentPart);

        ArgumentCaptor<List<Bom>> captor = ArgumentCaptor.forClass(List.class);
        verify(bomRepository).saveAll(captor.capture());

        // Verify that the BOM list is saved correctly
        List<Bom> savedBoms = captor.getValue();
        assertEquals(1, savedBoms.size());
        Bom savedBom = savedBoms.get(0);

        assertEquals(validChildPart, savedBom.getChildPart());
        assertEquals(parentPart, savedBom.getParentPart());
        assertEquals(5.0, savedBom.getQuantity());
    }

    @Test
    void testCreatePartCostEntity() throws Exception {
        // Setup the Part entity
        Part part = new Part();
        part.setPartId(1L);
        part.setPartName("Part A");

        // Setup the VendorCostDto with cost factor values
        CostFactorDto costFactorDto = new CostFactorDto(1L, "Cost Factor 1", 100.0);
        VendorCostDto vendorCostDto = new VendorCostDto(
                1L, "Vendor A", "123 Vendor St", "vendor@example.com", "123-456-7890",
                List.of(costFactorDto));

        Vendor vendor = new Vendor();
        vendor.setVendorId(1L);
        vendor.setVendorName("Vendor A");

        CostFactor costFactor = new CostFactor();
        costFactor.setFactorId(1L);
        costFactor.setFactorName("Cost Factor 1");

        Map<Long, Vendor> vendorMap = new HashMap<>();
        vendorMap.put(vendor.getVendorId(), vendor);

        Map<Long, CostFactor> costFactorMap = new HashMap<>();
        costFactorMap.put(costFactor.getFactorId(), costFactor);

        Method createPartCostEntityMethod = PartServiceImpl.class.getDeclaredMethod(
                "createPartCostEntity", Part.class, VendorCostDto.class, Map.class, Map.class);
        createPartCostEntityMethod.setAccessible(true); // Make it accessible

        PartCost partCost = (PartCost) createPartCostEntityMethod.invoke(
                partServiceImpl, part, vendorCostDto, vendorMap, costFactorMap);

        // Assertions
        assertNotNull(partCost);
        assertEquals(1L, partCost.getPart().getPartId());
        assertEquals("Part A", partCost.getPart().getPartName());
        assertEquals(1L, partCost.getVendor().getVendorId());
        assertEquals("Vendor A", partCost.getVendor().getVendorName());

        // Verify the cost factor list is not null and contains the correct data
        assertNotNull(partCost.getCostFactorList());
        assertEquals(1, partCost.getCostFactorList().size());

        // Test PartCostCostFactor associations
        PartCostCostFactor partCostCostFactor = partCost.getCostFactorList().get(0);
        assertNotNull(partCostCostFactor);
        assertEquals(1L, partCostCostFactor.getCostFactor().getFactorId());
        assertEquals("Cost Factor 1", partCostCostFactor.getCostFactor().getFactorName());
        assertEquals(100.0, partCostCostFactor.getValue(), 0.001); // Allow a small delta for floating-point comparisons
        assertEquals(partCost, partCostCostFactor.getPartCost());
    }

    @Test
    void testUpdatePartById_ForUnitPart_WhenCostIsChanged() {
        // UdatePartById when one vendor is removed and one cost factor value is changed
        Long partId = 1L;
        Long companyId = 500L;
        Long vendorId1 = 1L;
        Long vendorId2 = 2L;
        Long costFactorId = 2L;

        // Setup existing Part
        Part existingPart = new Part();
        existingPart.setPartId(partId);
        existingPart.setPartName("Eng1");
        existingPart.setType(PartType.UNIT);

        // Setup CostFactor
        CostFactor costFactor = new CostFactor();
        costFactor.setFactorId(costFactorId);
        costFactor.setFactorName("Cost Price");

        // Existing Vendor 1 (XYZ) with value = 300.0
        Vendor vendor1 = new Vendor();
        vendor1.setVendorId(vendorId1);
        vendor1.setVendorName("XYZ Company");

        PartCost partCost1 = new PartCost();
        partCost1.setVendor(vendor1);
        partCost1.setPart(existingPart);

        PartCostCostFactor pcf1 = new PartCostCostFactor();
        pcf1.setCostFactor(costFactor);
        pcf1.setValue(300.0);
        pcf1.setPartCost(partCost1);

        partCost1.setCostFactorList(List.of(pcf1));

        // Existing Vendor 2 (ABC) with value = 500.0
        Vendor vendor2 = new Vendor();
        vendor2.setVendorId(vendorId2);
        vendor2.setVendorName("ABC Company");

        PartCost partCost2 = new PartCost();
        partCost2.setVendor(vendor2);
        partCost2.setPart(existingPart);

        PartCostCostFactor pcf2 = new PartCostCostFactor();
        pcf2.setCostFactor(costFactor);
        pcf2.setValue(500.0);
        pcf2.setPartCost(partCost2);

        partCost2.setCostFactorList(List.of(pcf2));

        // Setup update request: only vendor1 remains, value updated to 100.0
        CostFactorDto updatedFactor = new CostFactorDto(costFactorId, "Cost Price", 100.0);

        VendorCostDto updatedVendor1 = new VendorCostDto(
                vendorId1, "XYZ Company", "address", "email", "9999999999", List.of(updatedFactor));

        PartRequestDto requestDto = new PartRequestDto();
        requestDto.setPartName("Eng1");
        requestDto.setType("UNIT");
        requestDto.setUnit("KG");
        requestDto.setVendorCostList(List.of(updatedVendor1));

        // Mocking
        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.of(existingPart));

        PartUnit mockUnit = new PartUnit();
        mockUnit.setUnitName("KG");

        when(partUnitRepository.findByUnitName("KG"))
                .thenReturn(Optional.of(mockUnit));

        when(partCostRepository.getRecentByPartId(partId))
                .thenReturn(List.of(partCost1, partCost2)); // Existing costs

        when(vendorRepository.findByVendorIdInAndCompany_CompanyId(Set.of(vendorId1), companyId))
                .thenReturn(List.of(vendor1));

        when(costFactorRepository.findByFactorIdInAndCompany_CompanyId(Set.of(costFactorId), companyId))
                .thenReturn(List.of(costFactor));

        doNothing().when(partPartAttributeRepository).deleteByPart_PartId(partId);
        doNothing().when(partPartAttributeRepository).flush();
        when(partPartAttributeRepository.findByPart(existingPart)).thenReturn(Collections.emptyList());


        // ArgumentCaptor to inspect saved and deleted data
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PartCost>> saveCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PartCost>> deleteCaptor = ArgumentCaptor.forClass(List.class);

        // Act
        partServiceImpl.updatePartById(partId, requestDto, companyId);

        // Assert saveAll was called with updated vendor1
        verify(partCostRepository).saveAll(saveCaptor.capture());
        List<PartCost> savedCosts = saveCaptor.getValue();

        assertEquals(1, savedCosts.size());
        assertEquals(vendorId1, savedCosts.get(0).getVendor().getVendorId());
        assertEquals(100.0, savedCosts.get(0).getCostFactorList().get(0).getValue());

        // Assert deleteAll was called with vendor2 (ABC Company)
        verify(partCostRepository).deleteAll(deleteCaptor.capture());
        List<PartCost> deletedCosts = deleteCaptor.getValue();

        assertEquals(1, deletedCosts.size());
        assertEquals(vendorId2, deletedCosts.get(0).getVendor().getVendorId());

        verify(partPartAttributeRepository).deleteByPart_PartId(partId);
        verify(partPartAttributeRepository).flush();
        verify(partPartAttributeRepository).findByPart(existingPart);

    }

    @Test
    void updatePartById_shouldUpdatePartSuccessfully() {
        // Setup mock data
        Long partId = 1L;
        Long companyId = 1L;

        // Prepare mock PartRequestDto with vendor cost list and BOM
        List<BomDto> bomList = new ArrayList<>();
        BomDto bomDto = BomDto.builder()
                .childPartId(2L) // Mock valid child part ID
                .quantity(10.00)
                .build();
        bomList.add(bomDto);

        PartRequestDto partRequestDto = PartRequestDto.builder()
                .partName("Updated Part")
                .partNumber("P12345")
                .type("MASTER")
                .unit("kg")
                .vendorCostList(new ArrayList<>()) // Mock an empty list
                .bom(bomList) // Ensure BOM list is provided
                .build();

        // Mock the existing part that should be returned by getValidatedPart
        Part existingPart = new Part();
        existingPart.setPartId(partId);
        existingPart.setPartNumber("P12345");
        existingPart.setPartName("Old Part");

        // Mock child part used in BOM
        Part childPart = new Part();
        childPart.setPartId(2L);
        childPart.setPartName("Child Part");

        // Mock repository behavior
        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.of(existingPart));

        when(partUnitRepository.findByUnitName(partRequestDto.getUnit()))
                .thenReturn(Optional.of(new PartUnit()));

        // Ensure child part exists for BOM validation
         when(partRepository.findByPartIdAndCompany_CompanyId(2L, companyId))
        .thenReturn(Optional.of(childPart));

        // Mock BOM save
        Bom newBom = new Bom(existingPart, childPart, bomDto.getQuantity());
        when(bomRepository.saveAll(anyList()))
                .thenReturn(Collections.singletonList(newBom));

        // Mock save of updated part
        when(partRepository.save(existingPart))
                .thenReturn(existingPart);


        // Mock BOM delete
        doNothing().when(bomRepository).deleteByParentPart(existingPart);

        // Call the service method
        PartDto updatedPartDto = partServiceImpl.updatePartById(partId, partRequestDto, companyId);

        // Verify interactions
        verify(partRepository, times(1)).save(existingPart);
        verify(partCostRepository, never()).saveAll(anyList());
        verify(bomRepository, times(1)).deleteByParentPart(existingPart);
        verify(bomRepository, times(1)).saveAll(anyList());

        // Assert result
        assertNotNull(updatedPartDto);
        assertEquals("Updated Part", updatedPartDto.getPartName());
    }

    @Test
   void updatePartById_shouldThrowWhenPartIsChildOfItself() {
    Long partId = 1L;
    Long companyId = 1L;

    // BOM contains a childPartId SAME as parent partId → triggers exception
    BomDto bomDto = BomDto.builder()
            .childPartId(partId)
            .quantity(5.0)
            .build();

    PartRequestDto request = PartRequestDto.builder()
            .partName("Parent Part")
            .type("MASTER")
            .unit("kg")
            .bom(List.of(bomDto))
            .build();

    // Mock existing part (parent)
    Part existingPart = new Part();
    existingPart.setPartId(partId);
    existingPart.setType(PartType.MASTER);

    when(partUnitRepository.findByUnitName(request.getUnit()))
            .thenReturn(Optional.of(new PartUnit()));

    // Mock finding the "child" — same ID as parent
    Part childPart = new Part();
    childPart.setPartId(partId); // same ID → triggers exception
    when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
            .thenReturn(Optional.of(existingPart)) // first call for parent
            .thenReturn(Optional.of(childPart));   // second call for child in BOM

    AppException ex = assertThrows(AppException.class, () -> {
        partServiceImpl.updatePartById(partId, request, companyId);
    });

    assertEquals(ErrorMessageConstant.PART_CANNOT_BE_CHILD_OF_ITSELF, ex.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
}

@Test
void savePartAttributes_shouldSaveAttributes_whenValidDataProvided() {
    Part part = new Part();
    PartAttribute attr = new PartAttribute();
    attr.setAttributeId(1L);
    attr.setDeleteFlag(0); // active

    AttributeValueDto attrDto = new AttributeValueDto();
    attrDto.setAttributeId(1L);
    attrDto.setValue("Red");

    PartRequestDto request = new PartRequestDto();
    request.setAttributeValueList(List.of(attrDto));

    when(partAttributeRepository.findByAttributeIdAndDeleteFlag(1L, 0)).thenReturn(Optional.of(attr));

    partServiceImpl.savePartAttributes(request, part);

    ArgumentCaptor<List<PartPartAttribute>> captor = ArgumentCaptor.forClass(List.class);
    verify(partPartAttributeRepository).saveAll(captor.capture());

    List<PartPartAttribute> savedAttributes = captor.getValue();
    assertEquals(1, savedAttributes.size());
    assertEquals("Red", savedAttributes.get(0).getAttributeValue());
}

@Test
void savePartAttributes_shouldDoNothing_whenAttributeListIsNull() {
    PartRequestDto request = new PartRequestDto();
    request.setAttributeValueList(null);

    partServiceImpl.savePartAttributes(request, new Part());

    verifyNoInteractions(partAttributeRepository);
    verifyNoInteractions(partPartAttributeRepository);
}

@Test
void savePartAttributes_shouldDoNothing_whenAttributeListIsEmpty() {
    PartRequestDto request = new PartRequestDto();
    request.setAttributeValueList(Collections.emptyList());

    partServiceImpl.savePartAttributes(request, new Part());

    verifyNoInteractions(partAttributeRepository);
    verifyNoInteractions(partPartAttributeRepository);
}

@Test
void savePartAttributes_shouldThrowException_whenAttributeIdIsNull() {
    AttributeValueDto attrDto = new AttributeValueDto();
    attrDto.setAttributeId(null);
    attrDto.setValue("Blue");

    PartRequestDto request = new PartRequestDto();
    request.setAttributeValueList(List.of(attrDto));

    AppException exception = assertThrows(AppException.class, () ->
        partServiceImpl.savePartAttributes(request, new Part())
    );

    assertEquals(ErrorMessageConstant.ATTRIBUTE_NULL, exception.getMessage());
}

@Test
void savePartAttributes_shouldThrowException_whenDuplicateAttributeIdExists() {
    AttributeValueDto attr1 = new AttributeValueDto();
    attr1.setAttributeId(1L);
    attr1.setValue("Red");

    AttributeValueDto attr2 = new AttributeValueDto();
    attr2.setAttributeId(1L);
    attr2.setValue("Green");

    PartRequestDto request = new PartRequestDto();
    request.setAttributeValueList(List.of(attr1, attr2));

    PartAttribute mockAttr = new PartAttribute();
    mockAttr.setAttributeId(1L);
    mockAttr.setDeleteFlag(0);
    when(partAttributeRepository.findByAttributeIdAndDeleteFlag(1L, 0)).thenReturn(Optional.of(mockAttr));

    AppException exception = assertThrows(AppException.class, () ->
        partServiceImpl.savePartAttributes(request, new Part())
    );

    assertEquals(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, exception.getMessage());
}

@Test
void savePartAttributes_shouldThrowException_whenAttributeIsNotFoundOrSoftDeleted() {
    AttributeValueDto attrDto = new AttributeValueDto();
    attrDto.setAttributeId(1L);
    attrDto.setValue("Yellow");

    PartRequestDto request = new PartRequestDto();
    request.setAttributeValueList(List.of(attrDto));

    when(partAttributeRepository.findByAttributeIdAndDeleteFlag(1L, 0)).thenReturn(Optional.empty());

    AppException exception = assertThrows(AppException.class, () ->
        partServiceImpl.savePartAttributes(request, new Part())
    );

    assertEquals(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, exception.getMessage());
}


    @Test
    void testGetPartsBasic() {
        int pageNo = 0;
        int pageSize = 2;
        String sortBy = "partId";
        Sorting sortMode = Sorting.ASC;
        long companyId = 123L;
        PartDto filter = new PartDto();

        Category category = Category.builder()
                .categoryId(1L) // or any valid ID
                .build();

        // Sample Vendor
        Vendor vendor = new Vendor();
        vendor.setVendorName("Vendor1");

        // Sample PartCost
        PartCost partCost = new PartCost();
        partCost.setVendor(vendor);

        // Sample Part
        Part part = new Part();
        part.setPartId(1L);
        part.setPartName("Gear");
        part.setPartNumber("G123");
        part.setCategory(category);
        part.setType(PartType.UNIT); // use enum
        part.setUnit("pcs");
        part.setPartCosts(new ArrayList<>(Set.of(partCost)));

        List<Part> partList = List.of(part);
        Page<Part> partPage = new PageImpl<>(partList, PageRequest.of(pageNo, pageSize), 1);

        when(partRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(partPage);
        when(partCostRepository.getMaxVendorCount()).thenReturn(3);

        ApiPageResponseDto<PartDataDto> response = partServiceImpl.getParts(filter, companyId, pageNo, pageSize, sortBy,
                sortMode);

        assertNotNull(response);
        assertEquals(1, response.getData().getPartsList().size());
        assertEquals("Gear", response.getData().getPartsList().get(0).getPartName());
        assertEquals(3, response.getData().getMaxVendorCount());
        assertEquals(1, response.getPageInfo().getTotalPages());

        verify(partRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testValidateCreatePartRequest_PartNumberExists() throws Exception {
        // Arrange
        PartRequestDto request = PartRequestDto.builder() // Using Lombok's builder
                .partName("Part A")
                .partNumber("P1234")
                .categoryId(1L)
                .type("Type A")
                .unit("kg")
                .vendorCostList(new ArrayList<>())
                .bom(new ArrayList<>())
                .build();
        Long companyId = 1L;
        Map<Long, Vendor> vendorMap = new HashMap<>(); // Setup vendor map
        Map<Long, CostFactor> costFactorMap = new HashMap<>(); // Setup cost factor map

        // Use reflection to invoke the private method
        Method method = PartServiceImpl.class.getDeclaredMethod("validateCreatePartRequest", PartRequestDto.class,
                Long.class, Map.class, Map.class);
        method.setAccessible(true);

        // Act & Assert
        try {
            method.invoke(partServiceImpl, request, companyId, vendorMap, costFactorMap);
            fail("Expected AppException was not thrown");
        } catch (InvocationTargetException e) {
            // Unwrap the exception
            Throwable cause = e.getCause();
            assertTrue(cause instanceof AppException);
            assertEquals("Invalid Category", cause.getMessage());
        }

    }

    @Test
    void testGetPartById() {
        Long partId = 1L;
        Long companyId = 100L;

        validatedPart = new Part();
        validatedPart.setPartId(partId);
        validatedPart.setPartName("Test Part");
        validatedPart.setPartNumber("TP-001");
        validatedPart.setUnit("kg");
        validatedPart.setType(PartType.MASTER);


        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.of(validatedPart));

        when(partCostRepository.getRecentByPartId(partId)).thenReturn(List.of());

        when(bomRepository.findByParentPart(validatedPart)).thenReturn(List.of());
        when(partPartAttributeRepository.findByPart(validatedPart)).thenReturn(List.of());

        PartDto result = partServiceImpl.getPartById(partId, companyId);

        assertNotNull(result, "The result should not be null");
    }

    @Test
    void testGetPartById_PartNotFound() {
        Long partId = 1L;
        Long companyId = 100L;

        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            partServiceImpl.getPartById(partId, companyId);
        });

        assertEquals(ErrorMessageConstant.PART_NOT_FOUND, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testCreatePartResponseDto() throws Exception {

        Part part = new Part();
        part.setPartId(1L);
        part.setPartName("Screw");
        part.setType(PartType.UNIT);
        // Other properties

        List<PartCost> partCostList = new ArrayList<>();
        List<Bom> bomList = new ArrayList<>();
        List<PartPartAttribute> attributeList = new ArrayList<>();

        Method method = PartServiceImpl.class.getDeclaredMethod("createPartResponseDto", Part.class, List.class,
                List.class, List.class);
        method.setAccessible(true);//overrides Java's access control checks at runtime.

        PartResponseDto dto = (PartResponseDto) method.invoke(partServiceImpl, part, partCostList, bomList, attributeList);

        assertNotNull(dto);
        assertEquals(1L, dto.getPartId());
        assertEquals("Screw", dto.getPartName());
    }

    @Test
    void testDownloadPartsToExcel() throws IOException {
        Long companyId = 1L;

        Company company = Company.builder()
                .companyId(companyId)
                .build();

        Category category = Category.builder()
                .categoryId(1L) // or any valid ID
                .build();

        Part part1 = Part.builder()
                .partNumber("P001")
                .partName("Part One")
                .unit("kg")
                .type(PartType.UNIT)
                .category(category)
                .company(company)
                .partCosts(Collections.emptyList())
                .build();

        Part part2 = Part.builder()
                .partNumber("P002")
                .partName("Part Two")
                .unit("m")
                .type(PartType.UNIT)
                .category(category)
                .company(company)
                .partCosts(Collections.emptyList())
                .build();

        when(partRepository.findByCompany_CompanyId(eq(companyId), any()))
                .thenReturn(Arrays.asList(part1, part2));

        byte[] mockExcelData = new byte[1]; // Simulated byte data
        when(excelService.generateSpreadsheet(any(), any())).thenReturn(mockExcelData);

        byte[] result = partServiceImpl.downloadPartsToExcel(companyId);

        assertNotNull(result, "The generated Excel data should not be null.");
        assertArrayEquals(mockExcelData, result, "The generated Excel data should match the expected byte array.");
    }

    @Test
    void testCreateVendorCostList() {
        // Mock Vendor
        Vendor vendor = new Vendor();
        vendor.setVendorId(1L);
        vendor.setVendorName("Vendor 1");
        vendor.setAddress("Vendor Address");
        vendor.setEmailId("vendor1@example.com");
        vendor.setContactNumber("1234567890");

        CostFactor costFactor = new CostFactor();
        costFactor.setFactorId(1L);
        costFactor.setFactorName("Factor A");

        PartCostCostFactor costFactorLink = new PartCostCostFactor();
        costFactorLink.setCostFactor(costFactor);
        costFactorLink.setValue(100.0);

        PartCost partCost = new PartCost();
        partCost.setVendor(vendor);
        partCost.setCostFactorList(Arrays.asList(costFactorLink));

        List<PartCost> partCostList = Arrays.asList(partCost);

        List<VendorCostDto> vendorCostList = partServiceImpl.createVendorCostList(partCostList);

        assertNotNull(vendorCostList);
        assertFalse(vendorCostList.isEmpty());

        VendorCostDto vendorCostDto = vendorCostList.get(0);
        assertEquals(1L, vendorCostDto.getId());
        assertEquals("Vendor 1", vendorCostDto.getVendorName());
        assertEquals("Vendor Address", vendorCostDto.getAddress());
        assertEquals("vendor1@example.com", vendorCostDto.getEmailId());
        assertEquals("1234567890", vendorCostDto.getContactNumber());

        assertNotNull(vendorCostDto.getCostFactorValues());
        assertEquals(1, vendorCostDto.getCostFactorValues().size());
        CostFactorDto costFactorDto = vendorCostDto.getCostFactorValues().get(0);
        assertEquals(1L, costFactorDto.getId());
        assertEquals("Factor A", costFactorDto.getFactorName());
        assertEquals(100.0, costFactorDto.getValue()); // Ensure the value is correctly set
    }

    @Test
    void testCreateVendorCostList_EmptyList() {

        List<PartCost> partCostList = new ArrayList<>();

        List<VendorCostDto> result = partServiceImpl.createVendorCostList(partCostList);
        assertTrue(result.isEmpty(), "The list should be empty when no PartCosts are provided.");
    }

    @Test
    void testDeletePartById_Valid() {
        Long partId = 1L;
        Long companyId = 2L;

        Part part = mock(Part.class);
        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.of(part));
        when(bomRepository.existsByChildPart(part)).thenReturn(false);

        partServiceImpl.deletePartById(partId, companyId);

        verify(partRepository).delete(part);
    }

    @Test
    void testDeletePartById_WhenPartIsChild_ThrowsException() {
        Long partId = 1L;
        Long companyId = 2L;

        Part part = mock(Part.class);
        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.of(part));
        when(bomRepository.existsByChildPart(part)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> {
            partServiceImpl.deletePartById(partId, companyId);
        });

        assertEquals(ErrorMessageConstant.RESTRICT_CHILD_PART_DELETE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        partRepository.delete((Part) part);

    }

    @Test
    void testGetPartCostsByPartAndVendor_Valid() {
        Long partId = 1L;
        Long vendorId = 2L;
        Long companyId = 3L;

        Part part = mock(Part.class);
        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
                .thenReturn(Optional.of(part));

        CostFactor costFactor = mock(CostFactor.class);
        when(costFactor.getFactorId()).thenReturn(10L);
        when(costFactor.getFactorName()).thenReturn("Material");

        PartCostCostFactor costFactorLink = mock(PartCostCostFactor.class);
        when(costFactorLink.getCostFactor()).thenReturn(costFactor);
        when(costFactorLink.getValue()).thenReturn(100.0);

        PartCost partCost = mock(PartCost.class);
        when(partCost.getUpdatedDateTime()).thenReturn(ZonedDateTime.now());
        when(partCost.getCostFactorList()).thenReturn(List.of(costFactorLink));

        List<PartCost> partCosts = List.of(partCost);
        when(partCostRepository.fetchByPartIdAndVendorId(partId, vendorId)).thenReturn(partCosts);

        CostHistoryResponseDto result = partServiceImpl.getPartCostsByPartAndVendor(partId, vendorId, companyId);

        assertNotNull(result);
        assertEquals(partId, result.getPartId());
        assertEquals(vendorId, result.getVendorId());
        assertEquals(1, result.getCostHistoryList().size());

        CostHistoryDto history = result.getCostHistoryList().get(0);
        assertEquals(1, history.getCostFactorList().size());

        CostFactorDto factorDto = history.getCostFactorList().get(0);
        assertEquals(10L, factorDto.getId());
        assertEquals("Material", factorDto.getFactorName());
        assertEquals(100.0, factorDto.getValue());
    }

    @Test
    void testGetPartUnits_EmptyPage_ShouldReturnEmptyList() {
        int pageNo = 0;
        int pageSize = 5;

        Page<PartUnit> emptyPage = new PageImpl<>(Collections.emptyList());

        when(partUnitRepository.findAll(PageRequest.of(pageNo, pageSize))).thenReturn(emptyPage);

        ApiPageResponseDto<List<PartUnitDto>> result = partServiceImpl.getPartUnits(pageNo, pageSize);

        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getPageInfo().getTotalRecords());
    }

    @Test
    void testDownloadBomPartListToExcel_EmptyBomList() throws IOException {
        // Arrange
        when(partRepository.findById(parentPartId)).thenReturn(Optional.of(part));
        when(bomRepository.findByParentPart_PartId(parentPartId)).thenReturn(Collections.emptyList());
        byte[] mockExcelData = new byte[] { 1, 2, 3 }; // Mock empty Excel data
        when(excelService.generateSpreadsheet(any(), any())).thenReturn(mockExcelData);

        // Act
        FileResponseDto response = partServiceImpl.downloadBomPartListToExcel(parentPartId, companyId);

        // Assert
        assertNotNull(response);
        assertEquals("P1234_Bo", response.getFileName().substring(0, 8));
    }

    @Test
    void testDownloadBomPartListToExcel_WithBomData() throws IOException {
        Long parentPartId = 1L;
        Long companyId = 100L;

        Company company = new Company();
        company.setCompanyId(companyId);

        Part parentPart = new Part();
        parentPart.setPartId(parentPartId);
        parentPart.setPartNumber("PARENT456");
        parentPart.setCompany(company);

        Part childPart = new Part();
        childPart.setPartId(2L);
        childPart.setPartNumber("CHILD1");
        childPart.setPartName("Child Part 1");

        Bom bom = new Bom();
        bom.setChildPart(childPart);
        bom.setQuantity(5.00);

        List<Bom> bomList = List.of(bom);

        when(partRepository.findById(parentPartId)).thenReturn(Optional.of(parentPart));
        when(bomRepository.findByParentPart_PartId(parentPartId)).thenReturn(bomList);
        when(excelService.generateSpreadsheet(anyList(), any())).thenReturn(new byte[] { 9, 9, 9 });

        FileResponseDto response = partServiceImpl.downloadBomPartListToExcel(parentPartId, companyId);

        assertNotNull(response);
        assertTrue(response.getFileName().contains("PARENT456_Bom_"));
        assertFalse(response.getFileData().isEmpty());
    }

    @Test
    void testDownloadFileFromS3_Success() {
        String s3Key = "company/parts/test.png";
        Long companyId = 1L;

        Part part = new Part();
        Company company = new Company();
        company.setCompanyId(companyId);
        part.setCompany(company);

        PartFile partFile = new PartFile();
        partFile.setS3FileKey(s3Key);
        partFile.setPart(part);

        when(partFileRepository.findByS3FileKey(s3Key)).thenReturn(Optional.of(partFile));
        when(s3Service.downloadFile(s3Key)).thenReturn("fileContent".getBytes());

        FileResponseDto response = partServiceImpl.downloadFileFromS3(s3Key, companyId);

        assertEquals("test.png", response.getFileName());
        assertEquals(Base64.getEncoder().encodeToString("fileContent".getBytes()), response.getFileData());
    }

    @Test
    void testUploadPartFile_ExceedsLimit_ThrowsException() {
        Long partId = 1L;
        Long companyId = 1L;

        PartFileUploadDto fileDto = new PartFileUploadDto();
        fileDto.setFileName("test.png");
        fileDto.setFileData(Base64.getEncoder().encodeToString("fileContent".getBytes()));

        Part part = new Part();
        part.setPartId(partId);
        Company company = new Company();
        company.setCompanyId(companyId);
        part.setCompany(company);

        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId)).thenReturn(Optional.of(part));
        when(partFileRepository.countByPart(part)).thenReturn(3); // Already max

        AppException exception = assertThrows(AppException.class, () -> {
            partServiceImpl.uploadPartFile(partId, fileDto, companyId);
        });

        assertEquals(ErrorMessageConstant.FILES_QUANTITY_EXCEEDS_LIMIT, exception.getMessage()); // Adjust if your
                                                                                                 // actual constant
                                                                                                 // message differs
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testDownloadFileFromS3_UnauthorizedOrNotFound() {
        String s3Key = "company/parts/test.png";
        Long companyId = 1L;

        when(partFileRepository.findByS3FileKey(s3Key)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            partServiceImpl.downloadFileFromS3(s3Key, companyId);
        });

        assertEquals(ErrorMessageConstant.FILE_NOT_FOUND_OR_UNAUTHORIZED, exception.getMessage()); // Adjust if constant
                                                                                                   // differs
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testGetPartFileUrls_Success() {
        Long partId = 1L;
        Long companyId = 1L;

        Part part = new Part();
        part.setPartId(partId);
        Company company = new Company();
        company.setCompanyId(companyId);
        part.setCompany(company);

        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId)).thenReturn(Optional.of(part));

        List<PartFile> mockFiles = List.of(
                PartFile.builder().s3FileKey("file1.png").build(),
                PartFile.builder().s3FileKey("file2.pdf").build());

        when(partFileRepository.findByPart(part)).thenReturn(mockFiles);

        List<String> urls = partServiceImpl.getPartFileUrls(partId, companyId);

        assertEquals(2, urls.size());
        assertTrue(urls.contains("file1.png"));
        assertTrue(urls.contains("file2.pdf"));
    }

    @Test
    void testUploadPartFile_Success() throws Exception {
        Long partId = 1L;
        Long companyId = 1L;

        Part part = new Part();
        Company company = new Company();
        company.setCompanyId(companyId);
        part.setCompany(company);

        PartFileUploadDto dto = new PartFileUploadDto();
        dto.setFileName("test.png");
        dto.setFileData(Base64.getEncoder().encodeToString("valid".getBytes()));

        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId)).thenReturn(Optional.of(part));
        when(partFileRepository.countByPart(part)).thenReturn(1);
        when(s3Service.uploadFile(partId, dto, companyId)).thenReturn("companyId/parts/1/test.png");

        String result = partServiceImpl.uploadPartFile(partId, dto, companyId);

        assertEquals("File uploaded successfully", result);
        verify(partFileRepository, times(1)).save(any(PartFile.class));
    }

    @Test
    void testUploadPartFile_DuplicateFile_ThrowsAppException() throws Exception {
        Long partId = 1L;
        Long companyId = 1L;

        Part part = new Part();
        Company company = new Company();
        company.setCompanyId(companyId);
        part.setCompany(company);

        PartFileUploadDto dto = new PartFileUploadDto();
        dto.setFileName("duplicate.png");
        dto.setFileData(Base64.getEncoder().encodeToString("content".getBytes()));

        when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId)).thenReturn(Optional.of(part));
        when(partFileRepository.countByPart(part)).thenReturn(1);
        when(s3Service.uploadFile(partId, dto, companyId)).thenReturn("companyId/parts/1/duplicate.png");
        doThrow(new DataIntegrityViolationException("Unique constraint")).when(partFileRepository).save(any());

        AppException exception = assertThrows(AppException.class, () -> {
            partServiceImpl.uploadPartFile(partId, dto, companyId);
        });

        assertEquals(ErrorMessageConstant.FILE_ALREADY_EXISTS, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testUploadFile_FileTooLarge_ThrowsAppException() {
        ReflectionTestUtils.setField(s3ServiceImpl, "maxFileSizeBytes", 5L); // override max size

        PartFileUploadDto dto = new PartFileUploadDto();
        dto.setFileName("bigfile.png");
        dto.setFileData(Base64.getEncoder().encodeToString("toolargecontent".getBytes()));

        AppException exception = assertThrows(AppException.class, () -> {
            s3ServiceImpl.uploadFile(1L, dto, 1L);
        });

        assertTrue(exception.getMessage().contains(ErrorMessageConstant.FILE_SIZE_EXCEEDS_LIMIT));
    }
  @Test
void updatePartById_shouldThrowWhenUnitInvalid() {
    Long partId = 1L, companyId = 1L;
        PartRequestDto request = PartRequestDto.builder() // Using Lombok's builder
                .unit("invalid")
                .build();

    when(partRepository.findByPartIdAndCompany_CompanyId(partId, companyId))
        .thenReturn(Optional.of(new Part()));
    when(partUnitRepository.findByUnitName("invalid"))
        .thenReturn(Optional.empty());

    assertThrows(AppException.class, () -> {
        partServiceImpl.updatePartById(partId, request, companyId);
    });
}

}

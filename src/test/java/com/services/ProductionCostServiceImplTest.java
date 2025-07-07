package com.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.dtos.CostItemDto;
import com.jubeiwato.costing_service.dtos.ProductionCostResponseDto;
import com.jubeiwato.costing_service.dtos.ProductionPlanPartDto;
import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.services.CostCalcService;
import com.jubeiwato.costing_service.services.impl.ProductionCostServiceImpl;

public class ProductionCostServiceImplTest {
     @Mock
    private PartRepository partRepository;

    @Mock
    private BomRepository bomRepository;

    @Mock
    private CostCalcService costCalcService;

    @InjectMocks
    private ProductionCostServiceImpl productionCostService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateProductionCost_forUnitPart() {
        // Given
        Long companyId = 1L;
        Long partId = 101L;
        Double quantity = 2.0;
        String priceMode = "MIN";

        Company company = new Company();
        company.setCompanyId(companyId);

        Part part = new Part();
        part.setPartId(partId);
        part.setType(PartType.UNIT);
        part.setCompany(company);

        ProductionPlanPartDto requestDto = ProductionPlanPartDto.builder()
                .partId(partId)
                .quantity(quantity)
                .build();

        CostItemDto costItemDto = CostItemDto.builder()
                .partName("Test Part")
                .partNumber("TP-101")
                .quantity(quantity)
                .rate(50.0)
                .vendorName("Test Vendor")
                .subTotal(100.0)
                .build();

        when(partRepository.findById(partId)).thenReturn(Optional.of(part));
        when(costCalcService.calculateUnitPart(partId, priceMode, quantity)).thenReturn(costItemDto);

        // When
        ProductionCostResponseDto result = productionCostService.calculateProductionCost(
                List.of(requestDto), priceMode, companyId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(100.0, result.getTotalCost());

        CostItemDto resultItem = result.getItems().get(0);
        assertEquals("Test Part", resultItem.getPartName());
        assertEquals("TP-101", resultItem.getPartNumber());
        assertEquals("Test Vendor", resultItem.getVendorName());
    }

    @Test
void testCalculateProductionCost_forMasterPart() {
    // Given
    Long companyId = 1L;
    Long masterPartId = 201L;
    Long unitPartId1 = 202L;
    Long unitPartId2 = 203L;
    Double masterQty = 1.0;
    String priceMode = "MIN";

    Company company = new Company();
    company.setCompanyId(companyId);

    Part masterPart = new Part();
    masterPart.setPartId(masterPartId);
    masterPart.setType(PartType.MASTER);
    masterPart.setCompany(company);

    Part unitPart1 = new Part();
    unitPart1.setPartId(unitPartId1);
    unitPart1.setType(PartType.UNIT);
    unitPart1.setCompany(company);

    Part unitPart2 = new Part();
    unitPart2.setPartId(unitPartId2);
    unitPart2.setType(PartType.UNIT);
    unitPart2.setCompany(company);

    // BOM entries for the master part
    Bom bom1 = new Bom();
    bom1.setParentPart(masterPart);
    bom1.setChildPart(unitPart1);
    bom1.setQuantity(2.0); // quantity of unitPart1 used in master

    Bom bom2 = new Bom();
    bom2.setParentPart(masterPart);
    bom2.setChildPart(unitPart2);
    bom2.setQuantity(3.0); // quantity of unitPart2 used in master

    ProductionPlanPartDto requestDto = ProductionPlanPartDto.builder()
            .partId(masterPartId)
            .quantity(masterQty)
            .build();

    CostItemDto costItemDto1 = CostItemDto.builder()
            .partName("Unit 1")
            .partNumber("U-001")
            .quantity(2.0)
            .rate(50.0)
            .vendorName("Vendor 1")
            .subTotal(100.0)
            .build();

    CostItemDto costItemDto2 = CostItemDto.builder()
            .partName("Unit 2")
            .partNumber("U-002")
            .quantity(3.0)
            .rate(30.0)
            .vendorName("Vendor 2")
            .subTotal(90.0)
            .build();

    when(partRepository.findById(masterPartId)).thenReturn(Optional.of(masterPart));
    when(bomRepository.findByParentPart_PartId(masterPartId)).thenReturn(List.of(bom1, bom2));

    when(costCalcService.calculateUnitPart(unitPartId1, priceMode, 2.0)).thenReturn(costItemDto1);
    when(costCalcService.calculateUnitPart(unitPartId2, priceMode, 3.0)).thenReturn(costItemDto2);

    // When
    ProductionCostResponseDto result = productionCostService.calculateProductionCost(
            List.of(requestDto), priceMode, companyId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getItems().size());
    assertEquals(190.0, result.getTotalCost());

    // Check individual items
    List<CostItemDto> items = result.getItems();
    assertTrue(items.stream().anyMatch(i -> i.getPartName().equals("Unit 1") && i.getSubTotal() == 100.0));
    assertTrue(items.stream().anyMatch(i -> i.getPartName().equals("Unit 2") && i.getSubTotal() == 90.0));
}

@Test
void testCalculateProductionCost_nestedMasterParts() {
    // IDs
    Long companyId = 1L;
    Long masterAId = 100L;
    Long masterBId = 101L;
    Long unitPart1Id = 201L;
    Long unitPart2Id = 202L;

    // Company
    Company company = new Company();
    company.setCompanyId(companyId);

    // MASTER A
    Part masterA = new Part();
    masterA.setPartId(masterAId);
    masterA.setType(PartType.MASTER);
    masterA.setCompany(company);

    // MASTER B
    Part masterB = new Part();
    masterB.setPartId(masterBId);
    masterB.setType(PartType.MASTER);
    masterB.setCompany(company);

    // UNIT 1
    Part unit1 = new Part();
    unit1.setPartId(unitPart1Id);
    unit1.setType(PartType.UNIT);
    unit1.setCompany(company);

    // UNIT 2
    Part unit2 = new Part();
    unit2.setPartId(unitPart2Id);
    unit2.setType(PartType.UNIT);
    unit2.setCompany(company);

    // BOM: MASTER A → MASTER B (qty = 3)
    Bom bomAtoB = new Bom();
    bomAtoB.setParentPart(masterA);
    bomAtoB.setChildPart(masterB);
    bomAtoB.setQuantity(3.0);

    // BOM: MASTER B → UNIT 1 (qty = 2), UNIT 2 (qty = 1)
    Bom bomBtoU1 = new Bom();
    bomBtoU1.setParentPart(masterB);
    bomBtoU1.setChildPart(unit1);
    bomBtoU1.setQuantity(2.0);

    Bom bomBtoU2 = new Bom();
    bomBtoU2.setParentPart(masterB);
    bomBtoU2.setChildPart(unit2);
    bomBtoU2.setQuantity(1.0);

    ProductionPlanPartDto request = ProductionPlanPartDto.builder()
            .partId(masterAId)
            .quantity(1.0)
            .build();

    // Cost items to be returned from service
    CostItemDto costItem1 = CostItemDto.builder()
            .partName("Unit 1")
            .partNumber("U1")
            .quantity(6.0) // 3 (A→B) * 2 (B→U1)
            .rate(50.0)
            .vendorName("V1")
            .subTotal(300.0)
            .build();

    CostItemDto costItem2 = CostItemDto.builder()
            .partName("Unit 2")
            .partNumber("U2")
            .quantity(3.0) // 3 (A→B) * 1 (B→U2)
            .rate(20.0)
            .vendorName("V2")
            .subTotal(60.0)
            .build();

    // Mocking DB calls
    when(partRepository.findById(masterAId)).thenReturn(Optional.of(masterA));
    when(bomRepository.findByParentPart_PartId(masterAId)).thenReturn(List.of(bomAtoB));
    when(bomRepository.findByParentPart_PartId(masterBId)).thenReturn(List.of(bomBtoU1, bomBtoU2));

    // Service mocks
    when(costCalcService.calculateUnitPart(unitPart1Id, "MIN", 6.0)).thenReturn(costItem1);
    when(costCalcService.calculateUnitPart(unitPart2Id, "MIN", 3.0)).thenReturn(costItem2);

    // Act
    ProductionCostResponseDto result = productionCostService.calculateProductionCost(
            List.of(request), "MIN", companyId);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getItems().size());
    assertEquals(360.0, result.getTotalCost());

    assertTrue(result.getItems().stream().anyMatch(i -> i.getPartName().equals("Unit 1") && i.getQuantity() == 6.0));
    assertTrue(result.getItems().stream().anyMatch(i -> i.getPartName().equals("Unit 2") && i.getQuantity() == 3.0));
}

}

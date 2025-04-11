package com.jubeiwato.costing_service;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.dtos.CostCalcResultDto;
import com.jubeiwato.costing_service.dtos.CostItemDto;
import com.jubeiwato.costing_service.entities.*;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.PartCostRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.services.impl.CostCalcServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CostCalcTests {
    @Mock
    private PartRepository partRepository;

    @Mock
    private PartCostRepository partCostRepository;

    @Mock
    private BomRepository bomRepository;

    @InjectMocks
    private CostCalcServiceImpl costCalcService;

    private Vendor vendor;
    private Company company;
    private Part unitPart;
    private PartCost partCost;
    private Part masterPart;
    private Part childUnitPart;
    private List<Bom> bomList;
    private PartCost childPartCost;
    private Part parentMaster;
    private Part nestedMaster;
    private Part nestedUnit;
    private PartCost nestedUnitCost;
    private Bom parentToNested;
    private Bom nestedToUnit;

    @BeforeEach
    void setUp() {

        company = new Company();
        company.setCompanyId(1L);

        vendor = new Vendor(1L, "Test Vendor", "", "", "", company);

        // Simple UNIT part
        unitPart = new Part();
        unitPart.setPartId(200L);
        unitPart.setPartName("Bolt");
        unitPart.setPartNumber("B123");
        unitPart.setCompany(company);
        unitPart.setType(PartType.UNIT);

        PartCostCostFactor factor1 = new PartCostCostFactor();
        factor1.setValue(10.0);
        partCost = new PartCost();
        partCost.setVendor(vendor);
        partCost.setCostFactorList(Collections.singletonList(factor1));

        // Master part with single UNIT child
        masterPart = new Part();
        masterPart.setPartId(500L);
        masterPart.setPartName("Engine Assembly");
        masterPart.setPartNumber("ENG500");
        masterPart.setCompany(company);
        masterPart.setType(PartType.MASTER);

        childUnitPart = new Part();
        childUnitPart.setPartId(501L);
        childUnitPart.setPartName("Spark Plug");
        childUnitPart.setPartNumber("SP501");
        childUnitPart.setCompany(company);
        childUnitPart.setType(PartType.UNIT);

        Bom bom = new Bom();
        bom.setParentPart(masterPart);
        bom.setChildPart(childUnitPart);
        bom.setQuantity(4.0);
        bomList = Collections.singletonList(bom);

        PartCostCostFactor factor2 = new PartCostCostFactor();
        factor2.setValue(20.0);
        childPartCost = new PartCost();
        childPartCost.setVendor(vendor);
        childPartCost.setCostFactorList(Collections.singletonList(factor2));

        // --------------------------------------------------------
        // Nested Structure Setup (parentMaster -> nestedMaster -> nestedUnit)

        // parentMaster
        parentMaster = new Part();
        parentMaster.setPartId(600L);
        parentMaster.setPartName("Car Body");
        parentMaster.setPartNumber("CAR600");
        parentMaster.setCompany(company);
        parentMaster.setType(PartType.MASTER);

        // nestedMaster
        nestedMaster = new Part();
        nestedMaster.setPartId(601L);
        nestedMaster.setPartName("Door Assembly");
        nestedMaster.setPartNumber("DOOR601");
        nestedMaster.setCompany(company);
        nestedMaster.setType(PartType.MASTER);

        // nestedUnit
        nestedUnit = new Part();
        nestedUnit.setPartId(602L);
        nestedUnit.setPartName("Handle");
        nestedUnit.setPartNumber("HD602");
        nestedUnit.setCompany(company);
        nestedUnit.setType(PartType.UNIT);

        // BOM: parentMaster -> nestedMaster
        parentToNested = new Bom();
        parentToNested.setParentPart(parentMaster);
        parentToNested.setChildPart(nestedMaster);
        parentToNested.setQuantity(2.0);

        // BOM: nestedMaster -> nestedUnit
        nestedToUnit = new Bom();
        nestedToUnit.setParentPart(nestedMaster);
        nestedToUnit.setChildPart(nestedUnit);
        nestedToUnit.setQuantity(3.0);

        // Combine all BOMs
        bomList = Arrays.asList(bom, parentToNested, nestedToUnit);

        // Cost for nestedUnit
        PartCostCostFactor nestedFactor = new PartCostCostFactor();
        nestedFactor.setValue(15.0);
        nestedUnitCost = new PartCost();
        nestedUnitCost.setVendor(vendor);
        nestedUnitCost.setCostFactorList(Collections.singletonList(nestedFactor));

    }

    @Test
    void testCalculatePrice_UnitPart_MinPrice() {
        when(partRepository.findById(200L)).thenReturn(Optional.of(unitPart));
        when(partCostRepository.findByPartId(200L)).thenReturn(Collections.singletonList(partCost));

        CostCalcResultDto result = costCalcService.calculatePrice(200L, "MIN", 1L);

        assertEquals(10.0, result.getTotalCost());
        assertEquals(1, result.getCostCalcDtoList().size());
        assertEquals("Bolt", result.getCostCalcDtoList().get(0).getPartName());
        assertEquals("Test Vendor", result.getCostCalcDtoList().get(0).getVendorName());
    }

    @Test
    void testCalculatePrice_ThrowsException_IfPartNotFound() {
        when(partRepository.findById(200L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> costCalcService.calculatePrice(200L, "MIN", 1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testCalculatePrice_ThrowsException_IfPartNotBelongToCompany() {
        Company anotherCompany = new Company();
        anotherCompany.setCompanyId(2L);
        unitPart.setCompany(anotherCompany);

        when(partRepository.findById(200L)).thenReturn(Optional.of(unitPart));

        AppException ex = assertThrows(AppException.class, () -> costCalcService.calculatePrice(200L, "MIN", 1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testCalculatePrice_InvalidPriceMode() {
        when(partRepository.findById(100L)).thenReturn(Optional.of(unitPart));
        unitPart.setCompany(company);

        AppException ex = assertThrows(AppException.class, () -> costCalcService.calculatePrice(100L, "INVALID", 1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testCalculatePrice_MasterPart_MaxPrice() {
        when(partRepository.findById(500L)).thenReturn(Optional.of(masterPart));
        when(partRepository.findById(501L)).thenReturn(Optional.of(childUnitPart));
        when(partRepository.findById(602L)).thenReturn(Optional.of(nestedUnit));

        when(bomRepository.findByParentPart_PartId(500L)).thenReturn(bomList);
        when(partCostRepository.findByPartId(501L)).thenReturn(Collections.singletonList(childPartCost));

        CostCalcResultDto result = costCalcService.calculatePrice(500L, "MAX", 1L);

        assertEquals(80.0, result.getTotalCost()); // 4 * 20.0
        assertEquals(3, result.getCostCalcDtoList().size());
        CostItemDto item = result.getCostCalcDtoList().get(0);
        assertEquals("Spark Plug", item.getPartName());
        assertEquals("SP501", item.getPartNumber());
        assertEquals(4.0, item.getQuantity());
        assertEquals(20.0, item.getRate());
        assertEquals(80.0, item.getSubTotal());
    }

    @Test
    void testCalculatePrice_RecursiveMasterPartHandling() {
        when(partRepository.findById(600L)).thenReturn(Optional.of(parentMaster));
        when(partRepository.findById(602L)).thenReturn(Optional.of(nestedUnit));

        when(bomRepository.findByParentPart_PartId(600L)).thenReturn(Collections.singletonList(parentToNested));
        when(bomRepository.findByParentPart_PartId(601L)).thenReturn(Collections.singletonList(nestedToUnit));

        when(partCostRepository.findByPartId(602L)).thenReturn(Collections.singletonList(nestedUnitCost));

        // Act
        CostCalcResultDto result = costCalcService.calculatePrice(600L, "AVG", 1L);

        // Assert
        assertEquals(90.0, result.getTotalCost()); // 2 doors * (3 handles * 15) = 2 * 45 = 90
        assertEquals(1, result.getCostCalcDtoList().size());

        CostItemDto item = result.getCostCalcDtoList().get(0);
        assertEquals("Door Assembly", item.getPartName());
        assertEquals("DOOR601", item.getPartNumber());
        assertEquals(2.0, item.getQuantity());
        assertEquals(45.0, item.getRate()); // rate is 3*15 = 45
        assertEquals(90.0, item.getSubTotal());
    }
}
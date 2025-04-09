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

    @BeforeEach
    void setUp() {

        company = new Company();
        company.setCompanyId(1L);

        vendor = new Vendor(1L, "Test Vendor", "", "", "", company);

        unitPart = new Part();
        unitPart.setPartId(200L);
        unitPart.setPartName("Bolt");
        unitPart.setPartNumber("B123");
        unitPart.setCompany(company);
        unitPart.setType(PartType.UNIT);

        PartCostCostFactor factor = new PartCostCostFactor();
        factor.setValue(10.0);

        partCost = new PartCost();
        partCost.setVendor(vendor);
        partCost.setCostFactorList(Collections.singletonList(factor));
    }

    @Test
    void testCalculatePrice_UnitPart_MinPrice() {
        when(partRepository.findById(100L)).thenReturn(Optional.of(unitPart));
        when(partCostRepository.findByPartId(100L)).thenReturn(Collections.singletonList(partCost));

        CostCalcResultDto result = costCalcService.calculatePrice(100L, "MIN", 1L);

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

        when(partRepository.findById(100L)).thenReturn(Optional.of(unitPart));

        AppException ex = assertThrows(AppException.class, () -> costCalcService.calculatePrice(100L, "MIN", 1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testCalculatePrice_InvalidPriceMode() {
        when(partRepository.findById(100L)).thenReturn(Optional.of(unitPart));
        unitPart.setCompany(company);

        AppException ex = assertThrows(AppException.class, () -> costCalcService.calculatePrice(100L, "INVALID", 1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}

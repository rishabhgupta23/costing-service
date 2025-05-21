package com.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;

import com.jubeiwato.costing_service.authentication.config.AppException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.PartAttribute;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.PartAttributeRepository;
import com.jubeiwato.costing_service.services.impl.PartAttributeServiceImpl;
import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.*;

public class PartAttributeServiceTest {

    @Mock
    private PartAttributeRepository partAttributeRepository;

    @Mock
    private CompanyRepository companyRepository;
    @InjectMocks
    private PartAttributeServiceImpl partAttributeService;

    private PartAttributeDto partAttributeDto;
    private Company company;
    private Long companyId = 1L;
    private PartAttribute partAttribute;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        company = new Company();
        company.setCompanyId(companyId);

        // This is the existing PartAttribute in DB
        partAttribute = new PartAttribute();
        partAttribute.setAttributeId(1L);
        partAttribute.setCompany(company);
        partAttribute.setAttributeName("OldName");
        partAttribute.setDeleteFlag(DeleteFlag.NEGATIVE.getValue());

        // This is the DTO with new attribute data for update
        partAttributeDto = new PartAttributeDto();
        partAttributeDto.setAttributeName("Test Attribute");

        // Inject mocks if not using @InjectMocks (optional if you already have
        // @InjectMocks)
        ReflectionTestUtils.setField(partAttributeService, "partAttributeRepository", partAttributeRepository);
        ReflectionTestUtils.setField(partAttributeService, "companyRepository", companyRepository);
    }

    @Test
    void testCreatePartAttribute_AttributeNameNull_ThrowsException() {
        PartAttributeDto dto = new PartAttributeDto();
        dto.setAttributeName(null);

        AppException ex = assertThrows(AppException.class, () -> {
            partAttributeService.createPartAttribute(dto, 1L);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(ErrorMessageConstant.PARTATTRIBUTE_MUST_BE_NOTNULL, ex.getMessage());
    }

    @Test
    void testCreatePartAttribute_CompanyNotFound_ThrowsException() {
        PartAttributeDto dto = new PartAttributeDto();
        dto.setAttributeName("Color");

        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> {
            partAttributeService.createPartAttribute(dto, 1L);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(ErrorMessageConstant.INVALID_COMPANY, ex.getMessage());
    }

    @Test
    void testCreatePartAttribute_AttributeExistsAndNotDeleted_ThrowsException() {
        partAttributeDto.setAttributeName("Color");

        Company company = new Company();
        company.setCompanyId(companyId);

        PartAttribute existing = new PartAttribute();
        existing.setDeleteFlag(DeleteFlag.NEGATIVE.getValue()); // Means active (not deleted)

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(partAttributeRepository.findByAttributeNameAndCompany_CompanyId("Color", companyId))
                .thenReturn(Optional.of(existing));

        AppException ex = assertThrows(AppException.class, () -> {
            partAttributeService.createPartAttribute(partAttributeDto, companyId);
        });

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, ex.getMessage());

        verify(companyRepository, times(1)).findById(companyId);
        verify(partAttributeRepository, times(1)).findByAttributeNameAndCompany_CompanyId("Color", companyId);
    }

    @Test
    void testCreatePartAttribute_AttributeExistsButDeleted_UpdatesAndReturns() {
        PartAttributeDto dto = new PartAttributeDto();
        dto.setAttributeName("Color");

        Company company = new Company();
        company.setCompanyId(1L);

        PartAttribute existing = new PartAttribute();
        existing.setDeleteFlag(DeleteFlag.POSITIVE.getValue()); // Deleted flag

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(partAttributeRepository.findByAttributeNameAndCompany_CompanyId("Color", 1L))
                .thenReturn(Optional.of(existing));

        when(partAttributeRepository.save(existing)).thenReturn(existing);

        PartAttribute result = partAttributeService.createPartAttribute(dto, 1L);

        assertEquals(DeleteFlag.NEGATIVE.getValue(), result.getDeleteFlag());
        verify(partAttributeRepository, times(1)).save(existing);
    }

    @Test
    void testCreatePartAttribute_NewAttribute_CreatesAndReturns() {
        PartAttributeDto dto = new PartAttributeDto();
        dto.setAttributeName("Color");

        Company company = new Company();
        company.setCompanyId(1L);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(partAttributeRepository.findByAttributeNameAndCompany_CompanyId("Color", 1L))
                .thenReturn(Optional.empty());

        PartAttribute savedAttribute = new PartAttribute();
        savedAttribute.setAttributeName("Color");
        savedAttribute.setCompany(company);

        when(partAttributeRepository.save(any(PartAttribute.class))).thenReturn(savedAttribute);

        PartAttribute result = partAttributeService.createPartAttribute(dto, 1L);

        assertEquals("Color", result.getAttributeName());
        assertEquals(company, result.getCompany());
        verify(partAttributeRepository, times(1)).save(any(PartAttribute.class));
    }

    @Test
    void testGetPartAttributeList_Success() {
        Long companyId = 1L;
        String attributeName = "color";
        int pageNo = 0;
        int pageSize = 2;
        String sortColumn = "attributeName";
        Sorting sortMode = Sorting.ASC;

        // Mocked PartAttribute entities
        PartAttribute partAttr1 = new PartAttribute();
        partAttr1.setAttributeName("Color");
        partAttr1.setAttributeId(1L);
        partAttr1.setCompany(company);

        PartAttribute partAttr2 = new PartAttribute();
        partAttr2.setAttributeName("Size");
        partAttr2.setAttributeId(2L);
        partAttr2.setCompany(company);

        List<PartAttribute> partAttributes = List.of(partAttr1, partAttr2);

        Page<PartAttribute> page = new PageImpl<>(partAttributes,
                PageRequest.of(pageNo, pageSize, Sort.by(sortColumn)),
                partAttributes.size());

        // Mock repository call
        when(partAttributeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Call method under test
        ApiPageResponseDto<List<PartAttributeDto>> response = partAttributeService
                .getPartAttributeList(companyId, attributeName, pageNo, pageSize, sortColumn, sortMode);

        // Validate the response content
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals("Color", response.getData().get(0).getAttributeName());
        assertEquals("Size", response.getData().get(1).getAttributeName());

        // Validate paging info
        assertEquals(1, response.getPageInfo().getTotalPages());
        assertEquals(pageNo, response.getPageInfo().getPageNumber());
        assertEquals(pageSize, response.getPageInfo().getPageSize());
        assertEquals(2, response.getPageInfo().getTotalRecords());
    }

    @Test
    void testGetPartAttributeList_ValidInput_ReturnsPage() {
        PartAttribute partAttribute = new PartAttribute();
        partAttribute.setAttributeName("Color");
        List<PartAttribute> list = List.of(partAttribute);
        Page<PartAttribute> page = new PageImpl<>(list);

        when(partAttributeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ApiPageResponseDto<List<PartAttributeDto>> response = partAttributeService.getPartAttributeList(
                1L,
                "Color",
                0,
                10,
                "attributeName",
                Sorting.ASC);

        assertNotNull(response);
        assertFalse(response.getData().isEmpty());
        assertEquals("Color", response.getData().get(0).getAttributeName());
    }

    @Test
    void testGetPartAttributeById_Valid() {
        PartAttribute attribute = new PartAttribute();
        attribute.setAttributeId(1L);
        attribute.setCompany(company);

        when(partAttributeRepository.findByAttributeIdAndCompany_CompanyId(1L, 1L)).thenReturn(Optional.of(attribute));

        PartAttribute result = partAttributeService.getPartAttributeById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getAttributeId());
    }

    @Test
    void testUpdatePartAttribute_NameConflict_ThrowsException() {
        Long attributeId = 1L;
        Long companyId = 1L;
        String newName = "Color";

        PartAttribute existing = new PartAttribute();
        existing.setAttributeId(attributeId);
        existing.setAttributeName("Size");
        existing.setDeleteFlag(DeleteFlag.NEGATIVE.getValue());
        existing.setCompany(company);

        PartAttributeDto dto = new PartAttributeDto();
        dto.setAttributeName(newName);

        when(partAttributeRepository.findByAttributeIdAndCompany_CompanyId(attributeId, companyId))
                .thenReturn(Optional.of(existing));
        when(partAttributeRepository.findByAttributeNameAndCompany_CompanyIdAndDeleteFlag(newName, companyId,
                DeleteFlag.NEGATIVE.getValue()))
                .thenReturn(Optional.of(new PartAttribute())); // Simulate conflict

        AppException ex = assertThrows(AppException.class, () -> {
            partAttributeService.updatePartAttribute(attributeId, dto, companyId);
        });

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, ex.getMessage());
    }

    @Test
    void testUpdatePartAttribute_SuccessfulUpdate() {
        Long attributeId = 1L;
        when(partAttributeRepository.findByAttributeIdAndCompany_CompanyId(attributeId, companyId))
                .thenReturn(Optional.of(partAttribute));

        when(partAttributeRepository.findByAttributeNameAndCompany_CompanyIdAndDeleteFlag("NewName", companyId,
                DeleteFlag.NEGATIVE.getValue()))
                .thenReturn(Optional.empty());

        when(partAttributeRepository.findByAttributeNameAndCompany_CompanyIdAndDeleteFlag("NewName", companyId,
                DeleteFlag.POSITIVE.getValue()))
                .thenReturn(Optional.empty());

        when(partAttributeRepository.save(any())).thenReturn(partAttribute);

        PartAttribute result = partAttributeService.updatePartAttribute(attributeId, partAttributeDto, companyId);

        assertNotNull(result);
        assertEquals("Test Attribute", result.getAttributeName());
    }

    @Test
    void testDeletePartAttribute_Success() {
        Long attributeId = 1L;
        // Mocking
        when(partAttributeRepository.findByAttributeIdAndCompany_CompanyId(attributeId, companyId))
                .thenReturn(Optional.of(partAttribute));

        // Action
        partAttributeService.deletePartAttribute(attributeId, companyId);

        // Verification
        assertEquals(DeleteFlag.POSITIVE.getValue(), partAttribute.getDeleteFlag());
        verify(partAttributeRepository).save(partAttribute);
    }

    @Test
    void testDeletePartAttribute_AlreadyDeleted_ThrowsException() {
        // Set already deleted
        partAttribute.setDeleteFlag(DeleteFlag.POSITIVE.getValue());

        Long attributeId = 1L;
        when(partAttributeRepository.findByAttributeIdAndCompany_CompanyId(attributeId, companyId))
                .thenReturn(Optional.of(partAttribute));

        AppException exception = assertThrows(AppException.class,
                () -> partAttributeService.deletePartAttribute(attributeId, companyId));

        assertEquals(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(partAttributeRepository, never()).save(any());
    }

    @Test
    void testDeletePartAttribute_NotFound_ThrowsException() {
        Long attributeId = 1L;
        when(partAttributeRepository.findByAttributeIdAndCompany_CompanyId(attributeId, companyId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> partAttributeService.deletePartAttribute(attributeId, companyId));

        assertEquals(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

}

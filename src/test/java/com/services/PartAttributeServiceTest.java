package com.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import com.jubeiwato.costing_service.authentication.config.AppException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;

import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.PartAttribute;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PartAttributeDto;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.PartAttributeRepository;
import com.jubeiwato.costing_service.services.impl.PartAttributeServiceImpl;
import com.jubeiwato.costing_service.utils.ValidationUtil;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired

    private PartAttributeDto partAttributeDto;
    private Company company;
    private Long companyId;
    private PartAttribute partAttribute;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        companyId = 1L;

        company = new Company();
        company.setCompanyId(1L);
        // Sample data for the tests
        partAttributeDto = new PartAttributeDto();
        partAttributeDto.setName("Test Attribute");

        partAttribute = new PartAttribute();
        partAttribute.setName("Test Attribute");
        partAttribute.setCompany(company);
        partAttribute.setAttributeId(1L);

        ReflectionTestUtils.setField(partAttributeService, "companyRepository", companyRepository);
        ReflectionTestUtils.setField(partAttributeService, "partAttributeRepository", partAttributeRepository);

    }

    @Test
    void testCreatePartAttribute_WithNullName_ShouldThrowException() {
        partAttributeDto.setName(null);

        AppException exception = assertThrows(AppException.class, () -> {
            partAttributeService.createPartAttribute(partAttributeDto, 1L);
        });

        assertEquals(ErrorMessageConstant.PARTATTRIBUTE_MUST_BE_NOTNULL, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testCreatePartAttribute_WithEmptyName_ShouldThrowException() {
        partAttributeDto.setName("  ");

        AppException exception = assertThrows(AppException.class, () -> {
            partAttributeService.createPartAttribute(partAttributeDto, 1L);
        });

        assertEquals(ErrorMessageConstant.PARTATTRIBUTE_MUST_BE_NOTNULL, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testCreatePartAttribute_WithInvalidCompany_ShouldThrowException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            partAttributeService.createPartAttribute(partAttributeDto, 1L);
        });

        assertEquals(ErrorMessageConstant.INVALID_COMPANY, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testCreatePartAttribute_WhenAttributeExists_ShouldThrowException() {
        // Arrange
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(partAttributeRepository.existsByNameAndCompany_CompanyId(partAttributeDto.getName(), companyId))
                .thenReturn(true);

        // Act & Assert
        AppException thrown = assertThrows(AppException.class, () -> {
            partAttributeService.createPartAttribute(partAttributeDto, companyId);
        });

        // Verify exception details
        assertEquals(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, thrown.getMessage());
        assertEquals(HttpStatus.CONFLICT, thrown.getStatus());

        // Verify that save method was not called
        verify(partAttributeRepository, times(0)).save(any());
    }

    @Test
    void testUpdatePartAttribute_Success() {
        // Mock repository to return an existing partAttribute
        when(partAttributeRepository.findById(1L)).thenReturn(Optional.of(partAttribute));
        when(partAttributeRepository.save(any(PartAttribute.class))).thenReturn(partAttribute);

        // Call the update method
        PartAttribute updatedAttribute = partAttributeService.updatePartAttribute(1L, partAttributeDto, companyId);

        // Verify interactions
        verify(partAttributeRepository).findById(1L);
        verify(partAttributeRepository).save(any(PartAttribute.class));

        // Assert that the name was updated
        assertEquals("Test Attribute", updatedAttribute.getName());
    }

    @Test
    void testGetPartAttributeById_WhenNotFound_ShouldThrowAppException() {
        // Arrange
        Long attributeId = 1L;

        when(partAttributeRepository.findById(attributeId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            partAttributeService.getPartAttributeById(attributeId);
        });

        assertEquals(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(partAttributeRepository, times(1)).findById(attributeId);
    }

    @Test
    void testGetPartAttributeList_SortingDescending() {
        // Arrange
        Long companyId = 1L;
        String name = "testName";
        int pageNo = 0;
        int pageSize = 10;
        String sortColumn = "name";
        Sorting sortMode = Sorting.DESC;

        // Mock a Page of PartAttribute
        PartAttribute partAttribute = new PartAttribute();
        partAttribute.setName("Test Attribute");
        partAttribute.setCompany(new Company());

        List<PartAttribute> partAttributes = List.of(partAttribute);
        Page<PartAttribute> page = new PageImpl<>(partAttributes);

        when(partAttributeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        ApiPageResponseDto<List<PartAttributeDto>> response = partAttributeService.getPartAttributeList(
                companyId, name, pageNo, pageSize, sortColumn, sortMode);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("Test Attribute", response.getData().get(0).getName());

        verify(partAttributeRepository, times(1))
                .findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testUpdatePartAttribute_InvalidName_ShouldThrowAppException() {
        // Test with an invalid (null or empty) name
        partAttributeDto.setName(null);

        // Mock repository to return an existing partAttribute
        when(partAttributeRepository.findById(1L)).thenReturn(Optional.of(partAttribute));

        // Call the update method and assert that the exception is thrown
        AppException exception = assertThrows(AppException.class,
                () -> partAttributeService.updatePartAttribute(1L, partAttributeDto, companyId));

        // Verify the exception message
        assertEquals(ErrorMessageConstant.PARTATTRIBUTE_MUST_BE_NOTNULL, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testUpdatePartAttribute_InvalidCompany_ShouldThrowAppException() {
        // Create a different company ID
        Long invalidCompanyId = 2L;

        // Mock repository to return an existing partAttribute
        when(partAttributeRepository.findById(1L)).thenReturn(Optional.of(partAttribute));

        // Call the update method and assert that the exception is thrown
        AppException exception = assertThrows(AppException.class,
                () -> partAttributeService.updatePartAttribute(1L, partAttributeDto, invalidCompanyId));

        // Verify the exception message
        assertEquals("Invalid company", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testUpdatePartAttribute_AttributeNotFound_ShouldThrowAppException() {
        // Mock repository to return an empty Optional (attribute not found)
        when(partAttributeRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the update method and assert that the exception is thrown
        AppException exception = assertThrows(AppException.class,
                () -> partAttributeService.updatePartAttribute(1L, partAttributeDto, companyId));

        // Verify the exception message
        assertEquals("Part attribute not found.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testGetPartAttributeList_ValiidInput() {
        List<PartAttribute> partAttributes = List.of(partAttribute); // Mocked entity list
        Page<PartAttribute> page = new PageImpl<>(partAttributes); // ✅ Fix: Now 'page' is defined

        when(partAttributeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ApiPageResponseDto<List<PartAttributeDto>> result = partAttributeService.getPartAttributeList(
                1L, "Test", 0, 10, "name", Sorting.ASC);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("Test Attribute", result.getData().get(0).getName());
    }

    @Test
    void testGetPartAttributeList_InvalidInput_ShouldThrowAppException() {
        try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
            mockedValidationUtil.when(() -> ValidationUtil.isValidInput("InvalidName"))
                    .thenReturn(false); // Simulate invalid input

            AppException exception = assertThrows(AppException.class,
                    () -> partAttributeService.getPartAttributeList(1L, "InvalidName", 0, 10, "name", Sorting.ASC));

            assertEquals(ErrorMessageConstant.INVALID_INPUT, exception.getMessage());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        }
    }

    @Test
    void testDeletePartAttribute() {
        // Mock the method that validates and retrieves the PartAttribute
        when(partAttributeRepository.findById(1L)).thenReturn(Optional.of(partAttribute));

        // Call the service method to delete
        partAttributeService.deletePartAttribute(1L);

        // Verify that the delete method was called on the repository with the correct
        // argument
        verify(partAttributeRepository, times(1)).delete(partAttribute);
    }

    @Test
    void testCreatePartAttribute_Success() {
        // Arrange
        Company company = new Company();
        company.setCompanyId(companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company)); // Mock company fetch
        when(partAttributeRepository.existsByNameAndCompany_CompanyId(partAttributeDto.getName(), companyId))
                .thenReturn(false); // No existing attribute with the same name

        PartAttribute savedPartAttribute = PartAttribute.builder()
                .name(partAttributeDto.getName())
                .company(company)
                .build();

        when(partAttributeRepository.save(any(PartAttribute.class))).thenReturn(savedPartAttribute); // Mock save

        // Act
        PartAttribute result = partAttributeService.createPartAttribute(partAttributeDto, companyId);

        // Assert
        assertNotNull(result); // Make sure the result is not null
        assertEquals(partAttributeDto.getName(), result.getName()); // Validate name
        assertEquals(companyId, result.getCompany().getCompanyId()); // Validate company
        verify(partAttributeRepository, times(1)).save(any(PartAttribute.class)); // Ensure save is called
    }

    @Test
    void testGetPartAttributeById_WhenFound_ShouldReturnPartAttribute() {
        // Arrange
        Long attributeId = 1L;
        PartAttribute partAttribute = new PartAttribute();
        partAttribute.setAttributeId(attributeId);
        partAttribute.setName("Test Attribute");

        // Mock the getValidatedPartAttribute method (if it's a private method)
        when(partAttributeRepository.findById(attributeId)).thenReturn(Optional.of(partAttribute)); // Mock the
                                                                                                    // repository method

        // Act
        PartAttribute result = partAttributeService.getPartAttributeById(attributeId);

        // Assert
        assertNotNull(result);
        assertEquals(attributeId, result.getAttributeId()); // Validate the returned ID
        assertEquals("Test Attribute", result.getName()); // Validate the returned name
    }

    @Test
    void testGetPartAttributeById_WhenNotFound_ShouldThrowException() {
        // Arrange
        Long attributeId = 1L;

        // Mock repository to return empty
        when(partAttributeRepository.findById(attributeId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            partAttributeService.getPartAttributeById(attributeId);
        });

        assertEquals(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testDeletePartAttribute_WhenNotFound_ShouldThrowException() {
        // Mock the method to return empty, simulating the case where PartAttribute is
        // not found
        when(partAttributeRepository.findById(1L)).thenReturn(Optional.empty());

        // Verify that the exception is thrown when trying to delete a non-existent
        // PartAttribute
        AppException exception = assertThrows(AppException.class, () -> {
            partAttributeService.deletePartAttribute(1L);
        });

        // Check that the exception message and status are as expected
        assertEquals("Part attribute not found.", exception.getMessage()); // Adjust message based on your
                                                                           // implementation
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

}

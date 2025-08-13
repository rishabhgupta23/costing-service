package com.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.CompanyDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.repositories.CompanyRepository; 
import com.jubeiwato.costing_service.services.impl.CompanyServiceImpl;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;



    @Test
    void createCompany_shouldThrowException_whenEmailAlreadyExists() {
        // Given
        String existingEmail = "test@example.com";
        Company existingCompany = new Company();
        existingCompany.setCompanyEmailId(existingEmail);

        // Mock repository to simulate existing company
        when(companyRepository.findByCompanyEmailId(existingEmail))
                .thenReturn(Optional.of(existingCompany));

        CompanyDto companyDto = CompanyDto.builder()
                .companyName("Test Company")
                .companyEmailId(existingEmail)
                .companyAddress("Some Address")
                .maxUsers(6)
                .build();

        // When + Then
        AppException exception = assertThrows(AppException.class, 
            () -> companyService.createCompany(companyDto)
        );

        // Assert exception details
        String expectedMessage = String.format(
                ErrorMessageConstant.COMPANY_ALREADY_EXISTS_TEMPLATE,
                existingEmail
        );
        org.junit.jupiter.api.Assertions.assertEquals(expectedMessage, exception.getMessage());
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // Verify repository interaction
        verify(companyRepository, times(1)).findByCompanyEmailId(existingEmail);
        verify(companyRepository, never()).save(any());
    }
}


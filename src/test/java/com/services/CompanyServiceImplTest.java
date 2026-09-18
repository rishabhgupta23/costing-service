package com.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.UserRoleConstants;
import com.jubeiwato.costing_service.dtos.CompanyDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.entities.UserRole;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.services.impl.CompanyServiceImpl;
import com.jubeiwato.costing_service.utils.ValidationUtil;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company sampleCompany;

    @BeforeEach
    void setup() {
        sampleCompany = createCompany(1L, "Old Company", "old@mail.com", "Old Address", 10);
    }

    // ---------- Helper Methods ----------
    private User createUserWithRole(String role, Company company) {
        User u = new User();
        UserRole r = new UserRole();
        r.setRoleName(role);
        u.setUserRole(r);
        u.setCompany(company);
        return u;
    }

    private Company createCompany(Long id, String name, String email, String address, int maxUsers) {
        Company c = new Company();
        c.setCompanyId(id);
        c.setCompanyName(name);
        c.setCompanyEmailId(email);
        c.setCompanyAddress(address);
        c.setMaxUsers(maxUsers);
        return c;
    }

    private CompanyDto createCompanyDto(Long id, String name, String email, String address, int maxUsers) {
        return CompanyDto.builder()
                .companyId(id)
                .companyName(name)
                .companyEmailId(email)
                .companyAddress(address)
                .maxUsers(maxUsers)
                .build();
    }

    // ---------- Tests ----------

    @Test
    void testGetAllCompanies() {
        when(companyRepository.findAll()).thenReturn(List.of(sampleCompany, createCompany(2L, "C2", "c2@mail.com", "Addr", 5)));

        List<CompanyDto> result = companyService.getAllCompanies();

        assertEquals(2, result.size());
        assertEquals("Old Company", result.get(0).getCompanyName());
    }

    @Test
    void testGetCompanyById_AdminWithSameCompany_Success() {
        User admin = createUserWithRole(UserRoleConstants.ADMIN, sampleCompany);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));

        CompanyDto result = companyService.getCompanyById(1L, admin);

        assertNotNull(result);
        assertEquals(1L, result.getCompanyId());
    }

    @Test
    void testGetCompanyById_AdminWithDifferentCompany_ThrowsForbidden() {
        User admin = createUserWithRole(UserRoleConstants.ADMIN, createCompany(99L, "Other", "other@mail.com", "Addr", 5));

        assertThrows(AppException.class, () -> companyService.getCompanyById(1L, admin));
    }

    @Test
    void testGetCompanyById_CompanyNotFound_ThrowsNotFound() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());
        User guest = createUserWithRole(UserRoleConstants.GUEST, null);

        AppException ex = assertThrows(AppException.class, () -> companyService.getCompanyById(99L, guest));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testCreateCompany_Success() {
        String email = "valid@mail.com";
        when(companyRepository.findByCompanyEmailId(email)).thenReturn(Optional.empty());

        assertTrue(ValidationUtil.isValidEmail(email));

        CompanyDto dto = createCompanyDto(null, "PQR", email, "Addr", 6);
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setCompanyId(1L);
            return c;
        });

        CompanyDto result = companyService.createCompany(dto);

        assertEquals("PQR", result.getCompanyName());
        assertEquals(email, result.getCompanyEmailId());
    }

    @Test
    void testCreateCompany_InvalidEmail_ThrowsException() {
        CompanyDto dto = createCompanyDto(null, "XYZ", "wrong-format", "Addr", 5);

        AppException ex = assertThrows(AppException.class, () -> companyService.createCompany(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(ErrorMessageConstant.INVALID_EMAIL_FORMAT, ex.getMessage());
    }

    @Test
    void testCreateCompany_EmailAlreadyExists_ThrowsException() {
        String email = "exists@mail.com";
        when(companyRepository.findByCompanyEmailId(email)).thenReturn(Optional.of(sampleCompany));

        CompanyDto dto = createCompanyDto(null, "Test", email, "Addr", 6);

        AppException ex = assertThrows(AppException.class, () -> companyService.createCompany(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains(email));
    }

    @Test
    void updateCompany_NotFound_ThrowsException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());
        User superAdmin = createUserWithRole(UserRoleConstants.SUPER_ADMIN, null);

        AppException ex = assertThrows(AppException.class,
                () -> companyService.updateCompanybyId(1L, createCompanyDto(null, "New", "new@mail.com", "Addr", 10), superAdmin));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void updateCompany_InvalidEmailFormat_ThrowsException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        User superAdmin = createUserWithRole(UserRoleConstants.SUPER_ADMIN, null);

        CompanyDto dto = createCompanyDto(null, "New", "bad-email", "Addr", 10);

        AppException ex = assertThrows(AppException.class,
                () -> companyService.updateCompanybyId(1L, dto, superAdmin));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void deleteCompany_NotFound_ThrowsException() {
        when(companyRepository.existsById(1L)).thenReturn(false);
        AppException ex = assertThrows(AppException.class, () -> companyService.deleteCompanybyId(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void deleteCompany_Success() {
        when(companyRepository.existsById(1L)).thenReturn(true);

        companyService.deleteCompanybyId(1L);

        verify(companyRepository).deleteById(1L);
    }
}


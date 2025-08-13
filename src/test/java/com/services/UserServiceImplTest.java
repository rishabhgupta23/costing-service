package com.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.UserRoleConstants;
import com.jubeiwato.costing_service.dtos.AdminResetPasswordDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.entities.UserRole;
import com.jubeiwato.costing_service.repositories.UserRepository;
import com.jubeiwato.costing_service.repositories.UserRoleRepository;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.services.impl.UserServiceImpl;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User currentUser;
    private User targetUser;
    private Company company;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        company = new Company();
        company.setCompanyId(1L);

        UserRole adminRole = new UserRole();
        adminRole.setRoleName(UserRoleConstants.ADMIN);

        UserRole maintainerRole = new UserRole();
        maintainerRole.setRoleName("MAINTAINER");

        currentUser = new User();
        currentUser.setUserId(10L);
        currentUser.setCompany(company);
        currentUser.setUserRole(adminRole);

        targetUser = new User();
        targetUser.setUserId(20L);
        targetUser.setCompany(company);
        targetUser.setUserRole(maintainerRole);
    }

    @Test
void shouldThrowWhenPasswordNotConfirmed() {
    AdminResetPasswordDto dto = new AdminResetPasswordDto();
    dto.setUserEmail("test@example.com");
    dto.setNewTempPassword("Password1!");
    dto.setConfirmTempPassword("DifferentPassword!");

    AppException ex = assertThrows(AppException.class,
            () -> userService.adminResetUserPassword(dto, currentUser));

    assertEquals(ErrorMessageConstant.PASSWORD_DO_NOT_MATCH, ex.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
}

   @Test
void shouldThrowWhenUserNotFound() {
    AdminResetPasswordDto dto = new AdminResetPasswordDto();
    dto.setUserEmail("test@example.com");
    dto.setNewTempPassword("Password1!");
    dto.setConfirmTempPassword("Password1!");

    when(userRepository.findByEmailId(dto.getUserEmail())).thenReturn(Optional.empty());

    AppException ex = assertThrows(AppException.class,
            () -> userService.adminResetUserPassword(dto, currentUser));

    assertEquals(ErrorMessageConstant.USER_DOES_NOT_EXIST, ex.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
}

@Test
void shouldThrowWhenResettingOwnPassword() {
    AdminResetPasswordDto dto = new AdminResetPasswordDto();
    dto.setUserEmail("test@example.com");
    dto.setNewTempPassword("password123");
    dto.setConfirmTempPassword("password123"); // match → isPasswordConfirmed() = true

    targetUser.setUserId(currentUser.getUserId()); // same user

    when(userRepository.findByEmailId(dto.getUserEmail())).thenReturn(Optional.of(targetUser));

    AppException ex = assertThrows(AppException.class,
            () -> userService.adminResetUserPassword(dto, currentUser));

    assertEquals(ErrorMessageConstant.PASSWORD_CANNOT_BE_CHANGED, ex.getMessage());
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
}

@Test
void shouldThrowWhenAdminDifferentCompany() {
    AdminResetPasswordDto dto = new AdminResetPasswordDto();
    dto.setUserEmail("test@example.com");
    dto.setNewTempPassword("password123");
    dto.setConfirmTempPassword("password123");

    Company differentCompany = new Company();
    differentCompany.setCompanyId(2L);
    targetUser.setCompany(differentCompany);

    when(userRepository.findByEmailId(dto.getUserEmail())).thenReturn(Optional.of(targetUser));

    AppException ex = assertThrows(AppException.class,
            () -> userService.adminResetUserPassword(dto, currentUser));

    assertEquals(ErrorMessageConstant.USER_DOES_NOT_EXIST, ex.getMessage());
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
}


@Test
void shouldUpdatePasswordSuccessfully() {
    AdminResetPasswordDto dto = new AdminResetPasswordDto();
    dto.setUserEmail("test@example.com");
    dto.setNewTempPassword("newPass123.");
    dto.setConfirmTempPassword("newPass123.");

    when(userRepository.findByEmailId(dto.getUserEmail())).thenReturn(Optional.of(targetUser));
    when(passwordEncoder.encode("newPass123.")).thenReturn("encodedPass");

    userService.adminResetUserPassword(dto, currentUser);

    assertEquals("encodedPass", targetUser.getPassword());
    assertTrue(targetUser.isResetRequired());
    verify(userRepository).save(targetUser);
}

@Test
void shouldSkipCompanyCheckForNonAdmin() {
    currentUser.getUserRole().setRoleName("SUPER_ADMIN"); // not admin

    AdminResetPasswordDto dto = new AdminResetPasswordDto();
    dto.setUserEmail("test@example.com");
    dto.setNewTempPassword("newPass");
    dto.setConfirmTempPassword("newPass");

    Company differentCompany = new Company();
    differentCompany.setCompanyId(2L);
    targetUser.setCompany(differentCompany);

    when(userRepository.findByEmailId(dto.getUserEmail())).thenReturn(Optional.of(targetUser));
    when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");

    userService.adminResetUserPassword(dto, currentUser);

    assertEquals("encodedPass", targetUser.getPassword());
    verify(userRepository).save(targetUser);
}
}
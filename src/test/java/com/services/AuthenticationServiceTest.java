package com.services;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.authentication.service.AuthenticationService;
import com.jubeiwato.costing_service.authentication.service.JwtService;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.LoginResponse;
import com.jubeiwato.costing_service.dtos.ResetPasswordDto;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.UserRepository;
import com.jubeiwato.costing_service.repositories.UserRoleRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserRoleRepository userRoleRepository;
    
    @Mock
    private CompanyRepository companyRepository;
    
    @InjectMocks
    private AuthenticationService authenticationService;
    

    private ResetPasswordDto createResetDto(String email, String oldPwd, String newPwd, String confirmPwd) {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setEmail(email);
        dto.setOldPassword(oldPwd);
        dto.setNewPassword(newPwd);
        dto.setConfirmPassword(confirmPwd);
        return dto;
    }

    @Test
    void testPasswordMismatch_shouldThrowException() {
        User user = new User();
        ResetPasswordDto dto = createResetDto("test@example.com", "oldPass", "newPass1", "newPass2");

        AppException ex = assertThrows(AppException.class, () ->
                authenticationService.resetPassword(user, dto));
        assertEquals(ErrorMessageConstant.PASSWORD_DO_NOT_MATCH, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void resetPassword_shouldThrowAppException_whenOldPasswordIsIncorrect() {
        // Arrange
        String email = "test@example.com";
        String oldPassword = "wrongOldPassword8.";
        String newPassword = "NewPassword@123";
        String confirmPassword = "NewPassword@123";

        ResetPasswordDto dto = createResetDto(email, oldPassword, newPassword, confirmPassword);

        User user = new User();
        user.setEmailId(email);
        user.setPassword("encodedOldPassword8.");

        
        when(authenticationManager.authenticate(Mockito.any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
        authenticationService.resetPassword(user, dto);
       });

       // Verify exception details
       assertEquals("Password Incorrect", exception.getMessage());
       assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));//Verifies that the AuthenticationManager.authenticate() was called — meaning the old password was re-authenticated before the new one was set.
    }


    @Test
    void testSuccessfulResetPassword_shouldReturnNewToken() {
        User user = new User();
        ResetPasswordDto dto = createResetDto("test@example.com", "correctOldPass", "newPass", "newPass");

        String encodedPassword = "encodedNewPass";
        String token = "generated.jwt.token";

        when(passwordEncoder.encode("newPass")).thenReturn(encodedPassword);
        when(jwtService.generateToken(user)).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        LoginResponse response = authenticationService.resetPassword(user, dto);

        assertEquals(token, response.getToken());
        assertEquals(3600L, response.getExpiresIn());
        assertFalse(user.isResetRequired());
        verify(userRepository).save(user);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }



}

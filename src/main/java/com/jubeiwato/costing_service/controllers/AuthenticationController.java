package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.authentication.service.AuthenticationService;
import com.jubeiwato.costing_service.authentication.service.JwtService;
import com.jubeiwato.costing_service.dtos.AdminResetPasswordDto;
import com.jubeiwato.costing_service.dtos.LoginResponse;
import com.jubeiwato.costing_service.dtos.LoginUserDto;
import com.jubeiwato.costing_service.dtos.RegisterUserDto;
import com.jubeiwato.costing_service.dtos.ResetPasswordDto;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.entities.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.jubeiwato.costing_service.constants.UserRoleConstants.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
        @PreAuthorize("hasRole('" + SUPER_ADMIN + "')")
    public ResponseEntity<UserDto> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
    UserDto userDto = UserDto.entityToDto(registeredUser);
    return ResponseEntity.ok(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .resetRequired(authenticatedUser.isResetRequired()) 
                .build();

        return ResponseEntity.ok(loginResponse);
    } 
     
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        authenticationService.resetPassword(dto);
      return ResponseEntity.ok("Password reset successful.");
   }
   
   @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
   @PostMapping("/admin/reset-user-password")
   public ResponseEntity<String> adminResetUserPassword(@Valid @RequestBody AdminResetPasswordDto dto, @AuthenticationPrincipal User authenticatedUser) {
       authenticationService.adminResetUserPassword(dto, authenticatedUser );
     return ResponseEntity.ok("User password has been reset.");
    }

}
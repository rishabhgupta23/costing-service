package com.jubeiwato.costing_service.authentication.service;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.LoginUserDto;
import com.jubeiwato.costing_service.dtos.RegisterUserDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.entities.UserRole;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.UserRepository;
import com.jubeiwato.costing_service.repositories.UserRoleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final UserRoleRepository userRoleRepository;
    private final CompanyRepository companyRepository;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            CompanyRepository companyRepository,
            UserRoleRepository userRoleRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
        this.companyRepository = companyRepository;
    }

    public User signup(RegisterUserDto input) {
        Company company = companyRepository.findByCompanyId(input.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_COMPANY, HttpStatus.BAD_REQUEST));
        UserRole role = userRoleRepository.findById(input.getRoleId())
                .orElseThrow(() -> new AppException(ErrorMessageConstant.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));

        if (input.getDisplayName() == null || input.getDisplayName().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.DISPLAY_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (input.getEmail() == null || input.getEmail().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.EMAIL_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (input.getRoleId() == null) {
            throw new AppException(ErrorMessageConstant.ROLE_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByEmailId(input.getEmail()).isPresent()) {
            throw new AppException(ErrorMessageConstant.USER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
        User user = User.builder()
                .emailId(input.getEmail())
                .displayName(input.getDisplayName())
                .password(passwordEncoder.encode(input.getPassword()))
                .company(company)
                .userRole(role)
                .build();
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        userRepository.findByEmailId(input.getEmail())
                .orElseThrow(() -> new AppException(ErrorMessageConstant.USER_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()));
        } catch (Exception ex) {
            throw new AppException(ErrorMessageConstant.PASSWORD_INCORRECT, HttpStatus.UNAUTHORIZED);
        }

        return userRepository.findByEmailId(input.getEmail())
                .orElseThrow();
    }

}
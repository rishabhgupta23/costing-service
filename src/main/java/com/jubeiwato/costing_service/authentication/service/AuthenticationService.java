package com.jubeiwato.costing_service.authentication.service;

import com.jubeiwato.costing_service.dtos.LoginUserDto;
import com.jubeiwato.costing_service.dtos.RegisterUserDto;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDto input) {
        User user = User.builder()
                .emailId(input.getEmail())
                .name(input.getFullName())
                .password(passwordEncoder.encode(input.getPassword()))
                .build();
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmailId(input.getEmail())
                .orElseThrow();
    }
}
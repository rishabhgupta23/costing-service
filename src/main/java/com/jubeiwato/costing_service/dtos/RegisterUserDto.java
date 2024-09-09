package com.jubeiwato.costing_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterUserDto {

    @NotNull
    @Email
    private String email;
    @NotNull
    private String password;
    private String fullName;
    
}
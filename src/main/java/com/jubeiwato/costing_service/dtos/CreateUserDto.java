package com.jubeiwato.costing_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserDto {
    
    @NotNull
    @Email
    private String emailId;
    @NotNull
    private String password;
    private String displayName;
    private Long roleId;
}

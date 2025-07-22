package com.jubeiwato.costing_service.dtos;

import lombok.Data;

@Data
public class LoginUserDto {
    private String email;
    private String password;
    private boolean resetRequired;
}
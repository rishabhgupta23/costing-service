package com.jubeiwato.costing_service.dtos;

import lombok.Data;

@Data
public class AdminResetPasswordDto {
      private String userEmail;
    private String newTempPassword;

}

package com.jubeiwato.costing_service.dtos;
import com.jubeiwato.validation.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDto {

    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Old password is required")
    private String oldPassword;
    
    @ValidPassword
    @NotBlank(message = "New password is required")
    private String newPassword;
    
    @NotBlank(message = "confirmPassword is required")
    private String confirmPassword;


        public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
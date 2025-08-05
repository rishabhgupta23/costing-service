package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResetPasswordDto {

    @NotBlank(message = "userEmail is required")
    private String userEmail;
    
    @ValidPassword
    @NotBlank(message = "newTempPassword is required")
    private String newTempPassword;
    
    @NotBlank(message = "confirmTempPassword is required")
    private String confirmTempPassword;

    public boolean isPasswordConfirmed() {
        return newTempPassword != null && newTempPassword.equals(confirmTempPassword);
    }

}

package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long userId;
    String displayName;
    @NotNull
    @Email
    private String emailId;
    private String password;
    private CompanyDto company;
    private Long roleId;
    private String roleName;   
    private boolean resetRequired;  
    


    public static UserDto entityToDto(User user) {
        return new UserDtoBuilder()
        .userId(user.getUserId())
        .emailId(user.getEmailId())
        .displayName(user.getDisplayName())
        .company(CompanyDto.entityToDto(user.getCompany()))
        .roleId(user.getUserRole().getRoleId())
        .roleName(user.getUserRole().getRoleName())
        .build();
    }
}

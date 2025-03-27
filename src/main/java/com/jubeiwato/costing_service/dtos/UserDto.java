package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long userId;
    private String name;
    private String emailId;
    private String username;
    private String role;
    private CompanyDto company;

    public static UserDto entityToDto(User user) {
        return new UserDtoBuilder()
                .userId(user.getUserId())
                .emailId(user.getEmailId())
                .username(user.getEmailId())
                .name(user.getName())
                .role(user.getUserRole().getRoleName())
                .company(CompanyDto.entityToDto(user.getCompany()))
                .build();
    }
}

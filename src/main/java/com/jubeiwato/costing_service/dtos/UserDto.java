package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    String name;
    String emailId;


    public static UserDto entityToDto(User user) {
        return new UserDtoBuilder()
        .emailId(user.getEmailId())
        .name(user.getName())
        .build();
    }
}

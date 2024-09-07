package com.jubeiwato.costing_service.services;

import com.jubeiwato.costing_service.dtos.UserDto;

public interface UserService {
    void createUser(UserDto user);

    UserDto getUserById(Long id);

    void updateUserById(Long id, String name, String emailId);

    void deleteUserById(Long id);
}

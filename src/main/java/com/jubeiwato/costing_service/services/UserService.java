package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.dtos.UserRoleDto;

public interface UserService {
    void createUser(UserDto user);

ApiPageResponseDto<List<UserDto>> getUserByCompanyId(
    String displayName, String emailId, String roleName, 
    int pageNo, int pageSize, String sortColumn, Sorting sortMode);

    UserDto getUserByEmailId(String email);

    public ApiPageResponseDto<List<UserRoleDto>> getUserRoles(int pageNo, int pageSize);

    UserDto updateUserById(Long userId, UserDto userDto);


    void deleteUserById(Long userId);
    public UserDto getUserById(Long userId);
}

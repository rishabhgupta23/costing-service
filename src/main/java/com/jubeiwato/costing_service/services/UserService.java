package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.dtos.UserRoleDto;

public interface UserService {
    void createUser(UserDto user, Long companyId);

ApiPageResponseDto<List<UserDto>> getUserByCompanyId(Long userId,
    String displayName, String emailId, String roleName, 
    int pageNo, int pageSize, String sortColumn, Sorting sortMode, Long companyId);

    UserDto getUserByEmailId(String email);

    public ApiPageResponseDto<List<UserRoleDto>> getUserRoles(int pageNo, int pageSize);

    UserDto updateUserById(Long userId, UserDto userDto,Long companyId);


    void deleteUserById(Long userId, Long companyId);
    public UserDto getUserById(Long userId, Long companyId);
}

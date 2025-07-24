package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.AdminResetPasswordDto;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CreateUserDto;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.dtos.UserRoleDto;
import com.jubeiwato.costing_service.entities.User;

public interface UserService {
    void createUser(CreateUserDto user, Long companyId, String currentUserRole);

ApiPageResponseDto<List<UserDto>> getUserListByCompany(Long userId,
    String displayName, String emailId, String roleName, 
    int pageNo, int pageSize, String sortColumn, Sorting sortMode, Long companyId);

    UserDto getUserByEmailId(String email);

    public ApiPageResponseDto<List<UserRoleDto>> getUserRoles(int pageNo, int pageSize);

    UserDto updateUserById(Long userId, CreateUserDto userDto,Long companyId, Long currentUserId);


    void deleteUserById(Long currentUserId, Long userIdToDelete, Long companyId);
    public UserDto getUserById(Long userId, Long companyId);

    void adminResetUserPassword(AdminResetPasswordDto input , User currentUser);
}

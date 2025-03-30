package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.constants.UserRoleEnum;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.dtos.UserRoleDto;
import com.jubeiwato.costing_service.services.UserService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole(T(com.jubeiwato.costing_service.constants.UserRoleEnum).ADMIN.getRoleName()) or " +
    "hasRole(T(com.jubeiwato.costing_service.constants.UserRoleEnum).SUPER_ADMIN.getRoleName())")
    public ResponseEntity<GeneralResponseDto> createUser(@RequestBody UserDto user) {
        userService.createUser(user);
        GeneralResponseDto response = new GeneralResponseDto("Successful", HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

@GetMapping("/users/company")
public ResponseEntity<ApiPageResponseDto<List<UserDto>>> getUserByCompany(
    @RequestParam(required = false) String fullName,
    @RequestParam(required = false) String emailId,
    @RequestParam(required = false) String roleName,
    @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
    @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
    @RequestParam(required = false, defaultValue = "userId") String sortColumn,
    @RequestParam(required = false, defaultValue = "ASC") Sorting sortMode) {

    ApiPageResponseDto<List<UserDto>> users = userService.getUserByCompanyId(
        fullName, emailId, roleName, pageNo, pageSize, sortColumn, sortMode);
    
    return ResponseEntity.ok(users);
}

@GetMapping("/users/roles")
public ResponseEntity<ApiPageResponseDto<List<UserRoleDto>>> getUserRoles(
        @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
        @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
        @RequestParam(defaultValue = "roleName") String sortColumn,
        @RequestParam(defaultValue = "ASC") Sorting sortMode) {

    ApiPageResponseDto<List<UserRoleDto>> response = userService.getUserRoles(pageNo, pageSize);
    return ResponseEntity.ok(response);
}



    @GetMapping("/whoami")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        UserDto userDto = UserDto.entityToDto(currentUser);
        return ResponseEntity.ok(userDto);
    }

    
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole(T(com.jubeiwato.costing_service.constants.UserRoleEnum).ADMIN.getRoleName()) or " +
    "hasRole(T(com.jubeiwato.costing_service.constants.UserRoleEnum).SUPER_ADMIN.getRoleName())")
    public ResponseEntity<UserDto> updateUserById(@PathVariable Long userId, @RequestBody UserDto userDto) {
        UserDto updatedUser = this.userService.updateUserById(userId, userDto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole(T(com.jubeiwato.costing_service.constants.UserRoleEnum).ADMIN.getRoleName()) or " +
            "hasRole(T(com.jubeiwato.costing_service.constants.UserRoleEnum).SUPER_ADMIN.getRoleName())")
    public ResponseEntity<GeneralResponseDto> deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        GeneralResponseDto response = new GeneralResponseDto("User deleted successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
    
    
    
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole(T(com.jubeiwato.costing_service.constants.UserRoleEnum).ADMIN.getRoleName()) or " +
    "hasRole(T(com.jubeiwato.costing_service.constants.UserRoleEnum).SUPER_ADMIN.getRoleName())")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }


}

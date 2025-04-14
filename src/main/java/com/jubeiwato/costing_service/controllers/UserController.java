package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CreateUserDto;
import com.jubeiwato.costing_service.dtos.GeneralResponseDto;
import org.springframework.security.core.Authentication;

import com.jubeiwato.costing_service.dtos.CompanyDto;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.dtos.UserRoleDto;
import com.jubeiwato.costing_service.services.UserService;
import static com.jubeiwato.costing_service.constants.UserRoleConstants.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
    public ResponseEntity<GeneralResponseDto> createUser(@RequestBody CreateUserDto user,
            @AuthenticationPrincipal User authenticatedUser) {
        String currentUserRole = authenticatedUser.getUserRole().getRoleName();
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        userService.createUser(user, companyId, currentUserRole);
        GeneralResponseDto response = new GeneralResponseDto("Successful", HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiPageResponseDto<List<UserDto>>> getUserListByCompanyId(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String emailId,
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = "userId") String sortColumn,
            @RequestParam(required = false, defaultValue = "ASC") Sorting sortMode,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        ApiPageResponseDto<List<UserDto>> users = userService.getUserListByCompany(authenticatedUser.getUserId(),
                fullName, emailId, roleName, pageNo, pageSize, sortColumn, sortMode, companyId);

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
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User authenticatedUser) {
        UserDto userDto = UserDto.entityToDto(authenticatedUser);
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
    public ResponseEntity<UserDto> updateUserById(@PathVariable Long userId, @RequestBody CreateUserDto userDto,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        Long currentUserId = authenticatedUser.getUserId();
        UserDto updatedUser = this.userService.updateUserById(userId, userDto, companyId, currentUserId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
    public ResponseEntity<GeneralResponseDto> deleteUser(@PathVariable Long userId,
            @AuthenticationPrincipal User authenticatedUser) {
        // The authenticated user performing the deletion
        Long currentUserId = authenticatedUser.getUserId();
        Long companyId = authenticatedUser.getCompany().getCompanyId();

        // Call the service method to delete the user
        userService.deleteUserById(currentUserId, userId, companyId);
        GeneralResponseDto response = new GeneralResponseDto("User deleted successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + SUPER_ADMIN + "')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId,
            @AuthenticationPrincipal User authenticatedUser) {
        Long companyId = authenticatedUser.getCompany().getCompanyId();
        UserDto user = userService.getUserById(userId, companyId);
        return ResponseEntity.ok(user);
    }
}

package com.jubeiwato.costing_service.services.impl;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.dtos.UserRoleDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.entities.UserRole;
import com.jubeiwato.costing_service.repositories.UserRepository;
import com.jubeiwato.costing_service.repositories.UserRoleRepository;
import com.jubeiwato.costing_service.services.UserService;
import com.jubeiwato.costing_service.services.UserSpecification;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;



    public UserServiceImpl(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }
 public void createUser(UserDto user) {
    // Get the logged-in user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();

    // Fetch the company from the logged-in user
    Company company = currentUser.getCompany();
    if (company == null) {
        throw new AppException("Current user does not belong to any company", HttpStatus.BAD_REQUEST);
    }

    // Fetch role using roleId received from frontend
    UserRole role = userRoleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new AppException("Role not found", HttpStatus.NOT_FOUND));

    // Create new user
    User userEntity = User.builder()
        .emailId(user.getEmailId())
        .displayName(user.getDisplayName())
        .password(passwordEncoder.encode(user.getPassword()))
        .company(company)  // Assign company from logged-in user
        .userRole(role)
        .build();

    // Set metadata
    userEntity.setCreatedBy(AppConstants.APP_USER_ID);
    userEntity.setCreatedDateTime(ZonedDateTime.now());
    userEntity.setUpdatedDateTime(ZonedDateTime.now());
    userEntity.setUpdatedBy(AppConstants.APP_USER_ID);

    // Save to database
    userRepository.save(userEntity);
}
    @Override
    public  ApiPageResponseDto<List<UserRoleDto>> getUserRoles(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<UserRole> userRolePage = userRoleRepository.findAll(pageable);

        List<UserRoleDto> userRoleDtos = userRolePage.getContent()
                .stream()
                .map(UserRoleDto::entityToDto)
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
                .pageNumber(pageNo)
                .pageSize(pageSize)
                .totalPages(userRolePage.getTotalPages())
                .totalRecords(userRolePage.getTotalElements())
                .build();

        return ApiPageResponseDto.<List<UserRoleDto>>builder()
                .data(userRoleDtos)
                .pageInfo(pageInfo)
                .build();
    }

@Override
public ApiPageResponseDto<List<UserDto>> getUserByCompanyId(
        String fullName, String emailId, String roleName, int pageNo, int pageSize, String sortColumn, Sorting sortMode) {

    // Get the authenticated user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();

    // Fetch users of the same company as the logged-in user
    Long companyId = currentUser.getCompany().getCompanyId();

    if ("roleName".equals(sortColumn)) {
        sortColumn = "role.roleName"; 
    }
    
    // Define sorting direction
    Sort.Direction direction = (sortMode == Sorting.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sort = Sort.by(direction, sortColumn);

    // Define pagination
    Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

    // Apply filters using Specification
    Specification<User> spec = UserSpecification.getFilteredUsers(companyId, fullName, emailId, roleName);
    Page<User> userPage = userRepository.findAll(spec, pageable);

    // Convert User entities to UserDto
    List<UserDto> userDtos = userPage.getContent()
            .stream()
            .map(UserDto::entityToDto)
            .toList(); // `.toList()` instead of `.collect(Collectors.toList())` (Java 16+)

    // Create PageInfoDto
    PageInfoDto pageInfo = PageInfoDto.builder()
        .totalPages(userPage.getTotalPages())
        .pageNumber(pageNo)
        .pageSize(pageSize)
        .totalRecords(userPage.getTotalElements())
        .build();

    // Return paginated response
    return ApiPageResponseDto.<List<UserDto>>builder()
        .data(userDtos)
        .pageInfo(pageInfo)
        .build();
}


    

@Override
public UserDto updateUserById(Long userId, UserDto userDto) {
    User userEntity = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    userEntity.setDisplayName(userDto.getDisplayName());
    userEntity.setEmailId(userDto.getEmailId());

    // Fetch and set role
    UserRole userRole = userRoleRepository.findById(userDto.getRoleId())
        .orElseThrow(() -> new RuntimeException("Role not found"));
    userEntity.setUserRole(userRole);

    User updatedUser = userRepository.save(userEntity); // Save and get updated entity
    return UserDto.entityToDto(updatedUser); // Convert updated entity to DTO and return
}


    @Override
    @Transactional
    public void deleteUserById(Long userId) {

        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getUserByEmailId(String emailId) {
        User userEntity = userRepository.findByEmailId(emailId)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.USER_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));

        return UserDto.entityToDto(userEntity);
    }
    
    @Override
public UserDto getUserById(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    return UserDto.entityToDto(user);
}


    
    
}

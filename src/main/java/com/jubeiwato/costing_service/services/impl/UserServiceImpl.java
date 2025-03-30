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
import com.jubeiwato.costing_service.constants.UserRoleEnum;
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

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();

    Company company = currentUser.getCompany();
    if (company == null) {
        throw new AppException(ErrorMessageConstant.COMPANY_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    UserRole role = userRoleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new AppException(ErrorMessageConstant.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

    User userEntity = User.builder()
        .emailId(user.getEmailId())
        .displayName(user.getDisplayName())
        .password(passwordEncoder.encode(user.getPassword()))
        .company(company)
        .userRole(role)
        .build();

    userRepository.save(userEntity);
}
    @Override
    public  ApiPageResponseDto<List<UserRoleDto>> getUserRoles(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<UserRole> userRolePage = userRoleRepository.findAll(pageable);

        List<UserRoleDto> userRoleDtos = userRolePage.getContent()
                .stream()
                .map(UserRoleDto::entityToDto)
                .filter(role -> UserRoleEnum.MAINTAINER.getRoleName().equalsIgnoreCase(role.getRoleName()) ||
                            UserRoleEnum.GUEST.getRoleName().equalsIgnoreCase(role.getRoleName()))
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
        String displayName, String emailId, String roleName, int pageNo, int pageSize, String sortColumn, Sorting sortMode) {


    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();

    Long companyId = currentUser.getCompany().getCompanyId();

    if ("roleName".equals(sortColumn)) {
        sortColumn = "role.roleName"; 
    }

    Sort.Direction direction = (sortMode == Sorting.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sort = Sort.by(direction, sortColumn);


    Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

    Specification<User> spec = UserSpecification.getFilteredUsers(companyId, displayName, emailId, roleName);
    Page<User> userPage = userRepository.findAll(spec, pageable);

    List<UserDto> userDtos = userPage.getContent()
            .stream()
            .map(UserDto::entityToDto)
            .toList(); 

    PageInfoDto pageInfo = PageInfoDto.builder()
        .totalPages(userPage.getTotalPages())
        .pageNumber(pageNo)
        .pageSize(pageSize)
        .totalRecords(userPage.getTotalElements())
        .build();

    return ApiPageResponseDto.<List<UserDto>>builder()
        .data(userDtos)
        .pageInfo(pageInfo)
        .build();
}


    

@Override
public UserDto updateUserById(Long userId, UserDto userDto) {
        // Validate mandatory fields
        if (userDto.getDisplayName() == null || userDto.getDisplayName().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.DISPLAY_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (userDto.getEmailId() == null || userDto.getEmailId().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.EMAIL_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (userDto.getRoleId() == null) {
            throw new AppException(ErrorMessageConstant.ROLE_REQUIRED, HttpStatus.BAD_REQUEST);
        }
    User userEntity = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException( ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.USER_NOT_FOUND_TEMPLATE, userId)));

    userEntity.setDisplayName(userDto.getDisplayName());
    userEntity.setEmailId(userDto.getEmailId());

    UserRole userRole = userRoleRepository.findById(userDto.getRoleId())
        .orElseThrow(() -> new RuntimeException(ErrorMessageConstant.ROLE_NOT_FOUND));
    userEntity.setUserRole(userRole);

    User updatedUser = userRepository.save(userEntity);
    return UserDto.entityToDto(updatedUser);
}


@Override
@Transactional
public void deleteUserById(Long userId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();

    User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException(ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.USER_NOT_FOUND_TEMPLATE, userId)));

    UserRoleEnum targetUserRole;
    try {
        targetUserRole = UserRoleEnum.valueOf(targetUser.getUserRole().getRoleName().toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new RuntimeException(ErrorMessageConstant.INVALID_USER_ROLE);
    }

    // Prevent deletion of Super Admins
    if (targetUserRole == UserRoleEnum.SUPER_ADMIN) {
        throw new RuntimeException(ErrorMessageConstant.SUPER_ADMIN_DELETE_ERROR);
    }

    UserRoleEnum currentUserRole = UserRoleEnum.valueOf(currentUser.getUserRole().getRoleName().toUpperCase());
    if (targetUserRole == UserRoleEnum.ADMIN && currentUserRole != UserRoleEnum.SUPER_ADMIN) {
        throw new RuntimeException(ErrorMessageConstant.ADMIN_DELETE_ERROR);
    }

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
            .orElseThrow(() -> new RuntimeException(ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.USER_NOT_FOUND_TEMPLATE, userId)));
    return UserDto.entityToDto(user);
}  
}
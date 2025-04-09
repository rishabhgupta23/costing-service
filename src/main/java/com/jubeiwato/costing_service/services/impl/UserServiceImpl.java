package com.jubeiwato.costing_service.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import static com.jubeiwato.costing_service.constants.UserRoleConstants.*;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CreateUserDto;
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
import com.jubeiwato.costing_service.utils.AuthUtil;

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
    private User getAndValidateUser(Long userId, Long companyId) {
        return  this.userRepository.findByUserIdAndCompany_CompanyId(userId, companyId)
            .orElseThrow(() -> new AppException(ErrorMessageConstant.getFormattedMessage(
                ErrorMessageConstant.USER_NOT_FOUND_TEMPLATE, userId), HttpStatus.NOT_FOUND));
    }

    private void validateUserInput(CreateUserDto user) {
        if (user.getDisplayName() == null || user.getDisplayName().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.DISPLAY_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (user.getEmailId() == null || user.getEmailId().trim().isEmpty()) {
            throw new AppException(ErrorMessageConstant.EMAIL_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (user.getRoleId() == null) {
            throw new AppException(ErrorMessageConstant.ROLE_REQUIRED, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateRoleAssignment(String currentUserRole, String targetUserRole) {
        String currentAuthority = AuthUtil.normalizeRole(currentUserRole);
        String targetAuthority = AuthUtil.normalizeRole(targetUserRole);
    
        if (SUPER_ADMIN.equals(targetAuthority)) {
            throw new AppException(ErrorMessageConstant.SUPER_ADMIN_CREATION_ERROR, HttpStatus.FORBIDDEN);
        }
    
        if (ADMIN.equals(targetAuthority) && !SUPER_ADMIN.equals(currentAuthority)) {
            throw new AppException(ErrorMessageConstant.ADMIN_CREATION_RESTRICTED, HttpStatus.FORBIDDEN);
        }
    }
    
    public void createUser(CreateUserDto user, Long companyId, String currentUserRole) {

        validateUserInput(user);
UserRole role = userRoleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new AppException(ErrorMessageConstant.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

        String newUserRole = role.getRoleName().toUpperCase();
        validateRoleAssignment(currentUserRole, newUserRole);

    User userEntity = User.builder()
        .emailId(user.getEmailId())
        .displayName(user.getDisplayName())
        .password(passwordEncoder.encode(user.getPassword()))
        .company(Company.builder().companyId(companyId).build())
        .userRole(role)
        .build();
    
    userRepository.save(userEntity);
}
@Override
public ApiPageResponseDto<List<UserRoleDto>> getUserRoles(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize);
    Page<UserRole> userRolePage = userRoleRepository.findAll(pageable);

    List<UserRoleDto> userRoleDtos = userRolePage.getContent()
            .stream()
            .map(UserRoleDto::entityToDto)
            .filter(role -> MAINTAINER.equalsIgnoreCase(role.getRoleName()) ||
                        GUEST.equalsIgnoreCase(role.getRoleName()))
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
    public ApiPageResponseDto<List<UserDto>> getUserListByCompany(Long userId,
            String displayName, String emailId, String roleName, int pageNo, int pageSize, String sortColumn, Sorting sortMode, Long companyId) {
        if (companyId == null) {
            throw new AppException(ErrorMessageConstant.COMPANY_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if ("roleName".equals(sortColumn)) {
            sortColumn = "userRole.roleName"; 
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
    public UserDto updateUserById(Long userId, CreateUserDto userDto, Long companyId,  Long currentUserId) {
        User userEntity = getAndValidateUser(userId, companyId);
        validateUserInput(userDto);

        User currentUser = userRepository.findByUserIdAndCompany_CompanyId(currentUserId, companyId)
        .orElseThrow(() -> new AppException(ErrorMessageConstant.UNAUTHORIZED_ACCESS, HttpStatus.NOT_FOUND));

    String currentUserRole = currentUser.getUserRole().getRoleName().toUpperCase();
    String targetUserRole = userEntity.getUserRole().getRoleName().toUpperCase();
    validateRoleAssignment(currentUserRole, targetUserRole);
        userEntity.setDisplayName(userDto.getDisplayName());
        userEntity.setEmailId(userDto.getEmailId());

        UserRole userRole = userRoleRepository.findById(userDto.getRoleId())
            .orElseThrow(() -> new AppException(ErrorMessageConstant.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

        userEntity.setUserRole(userRole);
        User updatedUser = userRepository.save(userEntity);
        return UserDto.entityToDto(updatedUser);
    }
    


    @Override
    @Transactional
    public void deleteUserById(Long currentUserId, Long userIdToDelete, Long companyId) {
    User targetUser = getAndValidateUser(userIdToDelete, companyId);
        String targetUserRole = targetUser.getUserRole().getRoleName().toUpperCase();
        if (SUPER_ADMIN.equals(targetUserRole)) {
         throw new AppException(ErrorMessageConstant.SUPER_ADMIN_DELETE_ERROR, HttpStatus.FORBIDDEN);
        }

    User currentUser = getAndValidateUser(currentUserId, companyId);

    String currentUserRole = currentUser.getUserRole().getRoleName().toUpperCase();
    if (ADMIN.equals(targetUserRole) && !SUPER_ADMIN.equals(currentUserRole)) {
        throw new AppException(ErrorMessageConstant.ADMIN_DELETE_ERROR, HttpStatus.FORBIDDEN);
    }
    userRepository.deleteById(userIdToDelete);
    }

    @Override
    public UserDto getUserByEmailId(String emailId) {
        User userEntity = userRepository.findByEmailId(emailId)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.USER_DOES_NOT_EXIST, HttpStatus.NOT_FOUND));

        return UserDto.entityToDto(userEntity);
    }

    @Override
    public UserDto getUserById(Long userId, Long companyId) {
        User user = getAndValidateUser(userId, companyId);
        return UserDto.entityToDto(user);
    }

}
package com.jubeiwato.costing_service.services.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.entities.User;
import com.jubeiwato.costing_service.exceptions.NotFoundException;
import com.jubeiwato.costing_service.repositories.UserRepository;
import com.jubeiwato.costing_service.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    final private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(UserDto user) {
        User userEntity = User.builder()
        .emailId(user.getEmailId())
        .name(user.getName())
        .build();
        userEntity.setCreatedBy(AppConstants.APP_USER_ID);
        userEntity.setCreatedDateTime(LocalDateTime.now());
        userEntity.setUpdatedDateTime(LocalDateTime.now());
        userEntity.setUpdatedBy(AppConstants.APP_USER_ID);
        userRepository.save(userEntity);
    }

    @Override
    public UserDto getUserById(Long id) {
        User userEntity = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User does not exist"));
        
        return UserDto.entityToDto(userEntity);
    }

    @Override
    public void updateUserById(Long id, String name, String emailId) {
        User userEntity = userRepository.getReferenceById(id);
        userEntity.setName(name);
        userEntity.setEmailId(emailId);

        userRepository.save(userEntity);
    }

    @Override
    public void deleteUserById(Long id) {
        User userEntity = userRepository.getReferenceById(id);
        userEntity.setDeleteFlag(DeleteFlag.POSTITVE.getValue());

        userRepository.save(userEntity);
    }

    

    
    
}

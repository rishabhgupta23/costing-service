package com.jubeiwato.costing_service.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.jubeiwato.costing_service.constants.AppConstants;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;


@CrossOrigin
@RestController
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody UserDto user) {
        userService.createUser(user);
        return new ResponseEntity<>("Successfull", HttpStatus.CREATED);
    }

    @GetMapping("/whoami")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto response = userService.getUserById(AppConstants.DEFAULT_USER_ID);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    
}

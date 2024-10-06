package com.jubeiwato.costing_service.controllers;

import com.jubeiwato.costing_service.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;
import com.jubeiwato.costing_service.dtos.UserDto;
import com.jubeiwato.costing_service.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;


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
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }


    
}

package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

}

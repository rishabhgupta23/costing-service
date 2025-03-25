package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailId(String emailId);

    List<User> findByCompany(Company company);
}

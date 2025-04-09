package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jubeiwato.costing_service.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailId(String email);
    List<User> findByCompanyCompanyId(Long companyId);
 Optional<User> findByUserIdAndCompany_CompanyId(Long userId,Long companyId);


}



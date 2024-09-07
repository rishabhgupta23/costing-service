package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}

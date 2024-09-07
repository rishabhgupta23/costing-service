package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Part;

public interface PartRepository extends JpaRepository<Part, Long>{

}

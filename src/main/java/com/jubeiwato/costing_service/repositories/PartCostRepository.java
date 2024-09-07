package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.PartCost;

public interface PartCostRepository extends JpaRepository<PartCost, Long> {

}

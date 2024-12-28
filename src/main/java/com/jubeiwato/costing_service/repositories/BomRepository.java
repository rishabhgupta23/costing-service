package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.ids.BomId;

public interface BomRepository extends JpaRepository<Bom, BomId> {

}

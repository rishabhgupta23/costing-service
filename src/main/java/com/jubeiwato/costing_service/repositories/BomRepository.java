package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.ids.BomId;

import java.util.List;

public interface BomRepository extends JpaRepository<Bom, BomId> {
    boolean existsByChildPart(Part childPart);
    List<Bom> findByParentPart(Part parentPart);

    List<Bom> findByParentPart_PartId(Long partId);

    void deleteByParentPart(Part parentPart);
}

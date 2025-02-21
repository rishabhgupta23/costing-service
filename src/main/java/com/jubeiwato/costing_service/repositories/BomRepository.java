package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.ids.BomId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BomRepository extends JpaRepository<Bom, BomId> {
    boolean existsByChildPart(Part childPart);
    List<Bom> findByParentPart(Part parentPart);

    void deleteByParentPart(Part parentPart);

    @Query("SELECT b.childPart.partId, b.quantity FROM Bom b WHERE b.parentPart.partId = :parentPartId")
    List<Long> findChildPartIdsByMasterPartId(@Param("parentPartId") Long parentPartId);

    @Query("SELECT b.quantity FROM Bom b WHERE b.parentPart.partId = :parentPartId AND b.childPart.partId = :childPartId")
    Integer findQuantityByParentAndChild(@Param("parentPartId") Long parentPartId, @Param("childPartId") Long childPartId);


}

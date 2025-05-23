package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartFileRepository extends JpaRepository<PartFile, Long> {
   
    int countByPart(Part part);
}
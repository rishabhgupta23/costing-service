package com.jubeiwato.costing_service.repositories;

import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartFileRepository extends JpaRepository<PartFile, Long> {
   
    int countByPart(Part part);

    List<PartFile> findByPart(Part part);
    Optional<PartFile> findByS3FileKey(String fileUrl);
}
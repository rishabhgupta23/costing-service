package com.jubeiwato.costing_service.services;
import java.io.IOException;
import org.springframework.dao.DataIntegrityViolationException;
import com.jubeiwato.costing_service.dtos.PartFileUploadDto;

public interface S3Service {

    String uploadFile(Long partId, PartFileUploadDto imageUploadDto, Long companyId) throws DataIntegrityViolationException, IOException;

    byte[] downloadFile(String s3Key);
}

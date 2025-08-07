package com.jubeiwato.costing_service.services;
import java.io.IOException;
import com.jubeiwato.costing_service.dtos.PartFileUploadDto;

public interface S3Service {

    void uploadFile(Long partId, PartFileUploadDto imageUploadDto, Long companyId, String key) throws IOException;

    byte[] downloadFile(String s3Key);

    void deleteFileFromS3(String s3FileKey);
}

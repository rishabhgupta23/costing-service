package com.jubeiwato.costing_service.services;
import com.jubeiwato.costing_service.dtos.PartFileUploadDto;

public interface S3Service {

    String uploadFile(Long partId, PartFileUploadDto imageUploadDto, Long companyId) throws Exception;

    byte[] downloadFile(String s3Key);
}

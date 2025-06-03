package com.jubeiwato.costing_service.services;
import com.jubeiwato.costing_service.dtos.PartImageUploadDto;

public interface S3Service {

    String uploadFile(Long partId, PartImageUploadDto imageUploadDto, Long companyId);

    byte[] downloadFile(String s3Key);
}

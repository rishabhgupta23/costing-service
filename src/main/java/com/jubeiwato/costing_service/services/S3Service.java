package com.jubeiwato.costing_service.services;
import org.springframework.core.io.Resource;

public interface S3Service {

    String uploadPartImageToS3(Long partId, String base64Image, Long companyId);

    public Resource downloadFileFromS3(String fileUrl);
}

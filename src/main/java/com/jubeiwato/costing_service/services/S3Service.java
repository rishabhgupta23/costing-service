package com.jubeiwato.costing_service.services;

public interface S3Service {

    String uploadPartImageToS3(Long partId, String base64Image, Long companyId);
}

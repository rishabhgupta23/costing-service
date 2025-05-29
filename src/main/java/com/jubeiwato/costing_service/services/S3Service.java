package com.jubeiwato.costing_service.services;
import org.springframework.core.io.Resource;
import com.jubeiwato.costing_service.dtos.PartImageUploadDto;

public interface S3Service {

    String uploadPartImageToS3(Long partId, PartImageUploadDto imageUploadDto, Long companyId);

    public Resource downloadFileFromS3(String fileUrl);
}

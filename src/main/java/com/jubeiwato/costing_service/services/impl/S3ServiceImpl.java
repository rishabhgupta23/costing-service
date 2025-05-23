package com.jubeiwato.costing_service.services.impl;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.services.S3Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${app.maxImageSizeBytes}")
    private long maxImageSizeBytes;

    private final S3Client s3Client;

    @Override
    public String uploadPartImageToS3(Long partId, String base64Image, Long companyId) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            if (imageBytes.length > maxImageSizeBytes) {
                throw new AppException(ErrorMessageConstant.IMAGE_SIZE_EXCEEDS_LIMIT, HttpStatus.BAD_REQUEST);
            }

            String key = companyId + "/parts/" + partId + "/" + System.currentTimeMillis() + ".jpg";

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("image/jpeg")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));

            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
        } catch (Exception e) {
            throw new AppException(ErrorMessageConstant.IMAGE_UPLOAD_FAILED + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


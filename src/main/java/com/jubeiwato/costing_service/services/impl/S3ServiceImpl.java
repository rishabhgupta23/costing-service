package com.jubeiwato.costing_service.services.impl;

import java.io.InputStream;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.PartImageUploadDto;
import com.jubeiwato.costing_service.services.S3Service;
import org.apache.tika.Tika;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${app.maxImageSizeBytes}")
    private long maxImageSizeBytes;

    private final S3Client s3Client;

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
        "image/png", "image/jpeg", "image/jpg", "image/gif",
        "application/pdf", "text/csv", "application/vnd.ms-powerpoint", "text/plain",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    @Override
    public String uploadPartImageToS3(Long partId, PartImageUploadDto partImageUploadDto, Long companyId) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(partImageUploadDto.getFileData());

            if (imageBytes.length > maxImageSizeBytes) {
                throw new AppException(ErrorMessageConstant.IMAGE_SIZE_EXCEEDS_LIMIT, HttpStatus.BAD_REQUEST);
            }

                    // Use Apache Tika to detect MIME type
        Tika tika = new Tika();
        String detectedMimeType = tika.detect(imageBytes);

        if (!ALLOWED_FILE_TYPES.contains(detectedMimeType)) {
            throw new AppException("Unsupported file type: " + detectedMimeType, HttpStatus.BAD_REQUEST);
        }

        String key = companyId + "/parts/" + partId + "/" + partImageUploadDto.getFileName() ;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(detectedMimeType)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));

            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
        } catch (Exception e) {
            throw new AppException(ErrorMessageConstant.IMAGE_UPLOAD_FAILED + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

     @Override
    public Resource downloadFileFromS3(String fileUrl) {        
        // Extract key from full URL
        String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        InputStream inputStream = s3Client.getObject(getObjectRequest);
        return new InputStreamResource(inputStream);
    }

}


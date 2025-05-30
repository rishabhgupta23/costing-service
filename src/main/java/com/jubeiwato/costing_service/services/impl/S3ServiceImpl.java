package com.jubeiwato.costing_service.services.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import org.apache.tika.metadata.Metadata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.tika.parser.ParseContext;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.PartImageUploadDto;
import com.jubeiwato.costing_service.services.S3Service;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;

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
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    @Override
    public String uploadPartImageToS3(Long partId, PartImageUploadDto partImageUploadDto, Long companyId) {
        try {
            byte[] imageBytes = decodeAndValidateImage(partImageUploadDto.getFileData());
    
            String detectedMimeType = detectMimeType(imageBytes);
    
            validateFileType(detectedMimeType);
    
            String fileName = generateFileName(partImageUploadDto.getFileName(), detectedMimeType);
            String key = companyId + "/parts/" + partId + "/" + fileName;
    
            uploadToS3(imageBytes, key, detectedMimeType);
    
            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
    
        } catch (Exception e) {
            throw new AppException(ErrorMessageConstant.IMAGE_UPLOAD_FAILED + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private byte[] decodeAndValidateImage(String base64ImageData) {
        byte[] imageBytes = Base64.getDecoder().decode(base64ImageData);
    
        if (imageBytes.length > maxImageSizeBytes) {
            throw new AppException(ErrorMessageConstant.IMAGE_SIZE_EXCEEDS_LIMIT, HttpStatus.BAD_REQUEST);
        }
    
        return imageBytes;
    }
    
    private String detectMimeType(byte[] imageBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();
    
            parser.parse(inputStream, new DefaultHandler(), metadata, context);
    
            return metadata.get(Metadata.CONTENT_TYPE);
        } catch (Exception parseEx) {
            throw new AppException(
                    ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.FILE_PARSE_FAILED, parseEx.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    private void validateFileType(String mimeType) {
        String normalizedMimeType = mimeType.split(";")[0].trim();
    
        if (!ALLOWED_FILE_TYPES.contains(normalizedMimeType)) {
            throw new AppException(
                    ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.UNSUPPORTED_FILE_TYPE, mimeType),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    private String generateFileName(String originalFileName, String mimeType) {
        String extension = "";
        try {
            extension = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
        } catch (Exception ignored) {
        }
    
        if (!originalFileName.contains(".") && !extension.isEmpty()) {
            originalFileName += extension;
        }
    
        return originalFileName;
    }
    
    private void uploadToS3(byte[] imageBytes, String key, String mimeType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(mimeType)
                .build();
    
        s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));
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


package com.jubeiwato.costing_service.services.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.apache.tika.metadata.Metadata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.tika.parser.ParseContext;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.PartFileUploadDto;
import com.jubeiwato.costing_service.services.S3Service;
import org.apache.tika.parser.AutoDetectParser;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${app.maxFileSizeBytes}")
    private long maxFileSizeBytes;

    private final S3Client s3Client;

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
        "image/png", "image/jpeg", "image/jpg", "image/gif",
        "application/pdf", "text/csv", "application/vnd.ms-powerpoint", "text/plain",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    @Override
    public String uploadFile(Long partId, PartFileUploadDto partFileUploadDto, Long companyId){
        try {
            byte[] imageBytes = decodeAndValidateFile(partFileUploadDto.getFileData());
    
            String detectedMimeType = detectMimeType(imageBytes);
    
            validateFileType(detectedMimeType);
    
            String fileName = partFileUploadDto.getFileName();
            String key = companyId + "/parts/" + partId + "/" + fileName;
    
            uploadToS3(imageBytes, key, detectedMimeType);
    
            return key;
    
        } catch (Exception e) {
            throw new AppException(ErrorMessageConstant.IMAGE_UPLOAD_FAILED + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private byte[] decodeAndValidateFile(String base64FileData) {
        byte[] imageBytes = Base64.getDecoder().decode(base64FileData);
    
        if (imageBytes.length > maxFileSizeBytes) {
            throw new AppException(ErrorMessageConstant.FILE_SIZE_EXCEEDS_LIMIT, HttpStatus.BAD_REQUEST);
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
    
    
    private void uploadToS3(byte[] imageBytes, String key, String mimeType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(mimeType)
                .build();
    
        s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));
    }

    @Override
    public byte[] downloadFile(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
    
            InputStream inputStream = s3Client.getObject(getObjectRequest);
            return inputStream.readAllBytes();
    
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppException(ErrorMessageConstant.FILE_READ_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ErrorMessageConstant.UNEXPECTED_ERROR_OCCURED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
@Override
public void deleteFileFromS3(String s3FileKey) {
    try {
        DeleteObjectRequest deleteRequest =
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3FileKey)
                        .build();

        s3Client.deleteObject(deleteRequest);

    } catch (Exception e) {
        throw new AppException(
                ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.FILE_DELETE_FAILED, s3FileKey),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

}


package com.jubeiwato.costing_service.utils;

public class S3KeyUtil {
    
    private S3KeyUtil() {
        // private constructor to prevent instantiation
    }

    public static String generatePartFileKey(Long companyId, Long partId, String fileName) {
        return companyId + "/parts/" + partId + "/" + fileName;
    }
}

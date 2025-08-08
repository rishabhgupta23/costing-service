package com.jubeiwato.costing_service.utils;

public class S3Util {
    
    private S3Util() {
        // private constructor to prevent instantiation
    }

    public static String generatePartFileKey(Long companyId, Long partId, String fileName) {
        return companyId + "/parts/" + partId + "/" + fileName;
    }
}

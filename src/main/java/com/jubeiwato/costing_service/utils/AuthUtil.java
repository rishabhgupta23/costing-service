package com.jubeiwato.costing_service.utils;

public class AuthUtil {
    public static String normalizeRole(String role) {
        return role.trim().toUpperCase().replace(" ", "_");
    }
}

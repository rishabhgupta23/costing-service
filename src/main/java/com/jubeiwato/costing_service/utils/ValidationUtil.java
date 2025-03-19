package com.jubeiwato.costing_service.utils;

public class ValidationUtil {
    public static boolean isValidInput(String input) {
        if (input == null || input.trim().isEmpty()) return true;
        String regex = ".*[';#%*/=].*";
        return !input.matches(regex);
    }
}

package com.jubeiwato.costing_service.utils;

public class ValidationUtil {

    private static final String EMAIL_REGEX =
    "^(?!.*\\.\\.)[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static boolean isValidInput(String input) {
        if (input == null || input.trim().isEmpty()) return true;
        String regex = ".*[';#%*/=].*";
        return !input.matches(regex);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }
}

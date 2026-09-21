package com.jubeiwato.costing_service.utils;

public final class SearchUtil {

    private SearchUtil() {
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean hasSearchCriteria(String... values) {
        for (String value : values) {
            if (isNotBlank(value)) {
                return true;
            }
        }
        return false;
    }
}
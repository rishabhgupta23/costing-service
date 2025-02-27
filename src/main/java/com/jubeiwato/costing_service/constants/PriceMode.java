package com.jubeiwato.costing_service.constants;

public enum PriceMode {
    MIN, MAX, AVG;

    public static PriceMode fromString(String mode) {
        for (PriceMode priceMode : values()) {
            if (priceMode.name().equalsIgnoreCase(mode)) {
                return priceMode;
            }
        }
        throw new IllegalArgumentException("Invalid price mode. Choose from MIN, MAX, AVG.");
    }
}

package com.jubeiwato.costing_service.constants;
import org.springframework.http.HttpStatus;
import com.jubeiwato.costing_service.configue.AppException;

public enum PriceMode {
    MIN, MAX, AVG;

    public static PriceMode fromString(String mode) {
        for (PriceMode priceMode : values()) {
            if (priceMode.name().equalsIgnoreCase(mode)) {
                return priceMode;
            }
        }
        throw new AppException("Invalid price mode. Choose from MIN, MAX, AVG.", HttpStatus.BAD_REQUEST);
    }
}

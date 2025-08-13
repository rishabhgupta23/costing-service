package com.jubeiwato.costing_service.constants;
import org.springframework.http.HttpStatus;
import com.jubeiwato.costing_service.authentication.config.AppException;

public enum PriceMode {
    MIN, MAX, AVG;

    public static PriceMode fromString(String mode) {
        for (PriceMode priceMode : values()) {
            if (priceMode.name().equalsIgnoreCase(mode)) {
                return priceMode;
            }
        }
        throw new AppException(ErrorMessageConstant.INVALID_PRICE_MODE, HttpStatus.BAD_REQUEST);
    }
}

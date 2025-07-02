package com.jubeiwato.costing_service.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRequestDto {
     @NotNull(message = "Part ID cannot be null")
    private Long partId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity must be positive")
    private Double quantity;
}

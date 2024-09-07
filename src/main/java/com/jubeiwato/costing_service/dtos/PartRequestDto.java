package com.jubeiwato.costing_service.dtos;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartRequestDto {
    @NotNull(message = "Part Name cannot be null or blank")
    @NotBlank(message = "Part Name cannot be null or blank")
    private String partName;
    
    @NotNull(message = "Part Number cannot be null or blank")
    @NotBlank(message = "Part Number cannot be null or blank")
    private String partNumber;

    @NotNull(message = "Invalid Category")
    @NotBlank(message = "Invalid Category")
    private Long categoryId;
    
    @NotNull(message = "Invalid Part Type")
    @NotBlank(message = "Invalid Part Type")
    private String partType;
    
    @NotNull(message = "Invalid Measuring Unit")
    @NotBlank(message = "Invalid Measuring Unit")
    private String partUnit;
    
    private Map<Long, PartCostDto> vendorCostMap; // <VendorId, Cost Details>
}

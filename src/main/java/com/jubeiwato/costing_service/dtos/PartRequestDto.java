package com.jubeiwato.costing_service.dtos;

import java.util.List;
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

    private Long categoryId;
    
    @NotNull(message = "Invalid Part Type")
    @NotBlank(message = "Invalid Part Type")
    private String type;
    
    @NotNull(message = "Invalid Measuring Unit")
    @NotBlank(message = "Invalid Measuring Unit")
    private String unit;
    
    private Map<Long, PartCostDto> vendorCostMap; // <VendorId, Cost Details>

    private List<BomDto> bom;
}

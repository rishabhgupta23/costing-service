package com.jubeiwato.costing_service.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartRequestDto {
    @NotBlank(message = "Part Name cannot be null or blank")
    private String partName;
    
    @NotBlank(message = "Part Number cannot be null or blank")
    private String partNumber;

    private Long categoryId;

    @NotBlank(message = "Invalid Part Type")
    private String type;

    @NotBlank(message = "Invalid Measuring Unit")
    private String unit;

    private List<VendorCostDto> vendorCostList;
    
    @Valid
    private List<BomDto> bom;
}

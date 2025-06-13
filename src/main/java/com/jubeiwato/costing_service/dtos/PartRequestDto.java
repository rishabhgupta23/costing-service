package com.jubeiwato.costing_service.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    private List<VendorCostDto> vendorCostList;

    private List<BomDto> bom;
}

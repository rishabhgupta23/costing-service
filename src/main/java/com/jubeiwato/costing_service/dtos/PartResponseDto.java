package com.jubeiwato.costing_service.dtos;
import com.jubeiwato.costing_service.constants.PartType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartResponseDto extends PartDto {
    private List<VendorCostDto> vendorCostList;

    private List<BomResponseDto> bom;

    @Builder(builderMethodName = "superBuilder")
    public PartResponseDto(Long partId, String partName, String partNumber, String categoryName, PartType type, String unit, List<VendorCostDto> vendorCostList, List<BomResponseDto> bom) {
        super(partId, partName, partNumber, categoryName, type, unit);
        this.vendorCostList = vendorCostList;
        this.bom = bom;
    }
}

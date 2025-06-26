package com.jubeiwato.costing_service.dtos;
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

        private List<AttributeValueDto> attributeValueList;

    private List<BomResponseDto> bom;

    @Builder(builderMethodName = "superBuilder")
    public PartResponseDto(Long partId, String partName, String partNumber, String categoryName, String type, String unit, List<VendorCostDto> vendorCostList, List<BomResponseDto> bom,List<AttributeValueDto> attributeValueList) {
        super(partId, partName, partNumber, categoryName, type, unit);
        this.vendorCostList = vendorCostList;
        this.bom = bom;
            this.attributeValueList = attributeValueList;
    }
}

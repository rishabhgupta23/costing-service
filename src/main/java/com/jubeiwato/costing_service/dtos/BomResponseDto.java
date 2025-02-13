package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.Bom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BomResponseDto extends BomDto {
    String childPartName;
    String childPartNumber;

    @Builder(builderMethodName="superBuilder")
    public BomResponseDto(Long childPartId, Double quantity, String childPartName, String childPartNumber) {
        super(childPartId, quantity);
        this.childPartName = childPartName;
        this.childPartNumber = childPartNumber;
    }

    public static BomResponseDto entityToDto(Bom bom) {
        return BomResponseDto.superBuilder()
                .childPartId(bom.getChildPart().getPartId())
                .childPartName(bom.getChildPart().getPartName())
                .childPartNumber(bom.getChildPart().getPartNumber())
                .quantity(bom.getQuantity())
                .build();
    }
}

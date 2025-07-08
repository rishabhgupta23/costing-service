package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.PartPartAttribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeValueDto {
    private Long attributeId;
    private String attributeName;
    private String value;
    private Integer deleteFlag;

public static AttributeValueDto entityToDto(PartPartAttribute partPartAttribute) {
    return AttributeValueDto.builder()
        .attributeId(partPartAttribute.getAttribute().getAttributeId())
        .attributeName(partPartAttribute.getAttribute().getAttributeName())
        .value(partPartAttribute.getAttributeValue())
        .deleteFlag(partPartAttribute.getAttribute().getDeleteFlag())
        .build();
}

}
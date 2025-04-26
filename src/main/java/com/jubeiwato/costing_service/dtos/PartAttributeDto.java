package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.PartAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartAttributeDto {
    private Long attributeId;
    private String name;

    public static PartAttributeDto entityToDto(PartAttribute partAttribute) {
        return PartAttributeDto.builder()
                .attributeId(partAttribute.getAttributeId())
                .name(partAttribute.getName())
                .build();
    }
}

package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.Part;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PartDto {
    private Long partId;
    private String partName;
    private String partNumber;
    private String categoryName;
    private String type;
    private String unit;


    public static PartDto entityToDto(Part entity) {
        return PartDto.builder()
        .partId(entity.getPartId())
        .partName(entity.getPartName())
        .partNumber(entity.getPartNumber())
        .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
        .type(entity.getType().name())
        .unit(entity.getUnit())
        .build();
    }

}

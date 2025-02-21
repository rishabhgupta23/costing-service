package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;
import com.jubeiwato.costing_service.entities.Part;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private PartType type;
    private PartUnit unit;


    public static PartDto enitityToDto(Part entity) {
        return PartDto.builder()
        .partId(entity.getPartId())
        .partName(entity.getPartName())
        .partNumber(entity.getPartNumber())
        .categoryName(entity.getCategoryName())
        .type(entity.getType())
        .unit(entity.getUnit())
        .build();
    }

}

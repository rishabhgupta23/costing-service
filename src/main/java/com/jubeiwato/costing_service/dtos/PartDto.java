package com.jubeiwato.costing_service.dtos;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;
import com.jubeiwato.costing_service.entities.Part;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartDto {
    private Long partId;
    private String partName;
    private String partNumber;
    private String categoryName;
    private PartType type;
    private PartUnit unit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VendorDto> vendors;

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

    // Overloaded method to include vendors when needed
    public static PartDto entityToDto(Part entity, List<VendorDto> vendors) {
        return PartDto.builder()
            .partId(entity.getPartId())
            .partName(entity.getPartName())
            .partNumber(entity.getPartNumber())
            .categoryName(entity.getCategoryName())
            .type(entity.getType())
            .unit(entity.getUnit())
            .vendors(vendors) // Only set when needed
            .build();
    }
}

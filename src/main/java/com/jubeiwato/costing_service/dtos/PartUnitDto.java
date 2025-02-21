package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.PartUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartUnitDto {
    private String unitName;

    
    public static PartUnitDto entityToDto(PartUnit unit) {
        return PartUnitDto.builder()
                .unitName(unit.getUnitName())
                .build();
    }
}

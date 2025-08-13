package com.jubeiwato.costing_service.dtos;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PartRowDto extends PartDto {
    private List<String> vendorNames;


    @Builder(builderMethodName = "superBuilder")
    public PartRowDto(Long partId, String partName, String partNumber, String categoryName, String type,
            String unit, List<String> vendorNames) {
        super(partId, partName, partNumber, categoryName, type, unit);
        this.vendorNames = vendorNames;
    }
     
    
}

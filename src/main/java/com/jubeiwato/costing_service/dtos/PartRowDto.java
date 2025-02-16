package com.jubeiwato.costing_service.dtos;

import java.util.List;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PartRowDto extends PartDto {
    private List<String> vendorNames;


    @Builder(builderMethodName = "superBuilder")
    public PartRowDto(Long partId, String partName, String partNumber, String categoryName, PartType type,
            PartUnit unit, List<String> vendorNames) {
        super(partId, partName, partNumber, categoryName, type, unit);
        this.vendorNames = vendorNames;
    }
     
        public static PartRowDto fromQueryResult(Object[] obj) {
           
            return PartRowDto.superBuilder()
                .partId(((Number) obj[0]).longValue())
                .partName((String) obj[1])
                .partNumber((String) obj[2])
                .categoryName((String) obj[3])
                .type(PartType.valueOf((String) obj[4])) 
                .unit(PartUnit.valueOf((String) obj[5]))  
                .vendorNames(obj[6] != null ? Arrays.asList(((String) obj[6]).split(",")) : List.of()) 
                .build();
        }
    
}

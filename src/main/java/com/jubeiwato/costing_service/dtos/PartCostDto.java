package com.jubeiwato.costing_service.dtos;

import java.util.Map;
import java.util.stream.Collectors;

import com.jubeiwato.costing_service.entities.CostFactor;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartCostDto {
    private Long vendorId;
    private Map<CostFactorDto, Double> costFactorValues; //<CostFactorId, Value>

    public static PartCostDto entityToDto(PartCost partCost) {
        return PartCostDto.builder()
                .vendorId(partCost.getVendor().getVendorId())
                .costFactorValues(
                        partCost.getCostFactorList().stream()
                                .collect(Collectors.toMap(
                                        costFactorValue -> CostFactorDto.entityToDto(costFactorValue.getCostFactor()),
                                        PartCostCostFactor::getValue
                                ))
                )
                .build();
    }
}

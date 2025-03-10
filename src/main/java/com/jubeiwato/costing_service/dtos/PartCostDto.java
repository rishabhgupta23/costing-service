package com.jubeiwato.costing_service.dtos;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartCostDto {
    private Long vendorId;
    private Map<CostFactorDto, Double> costFactorValues; //<CostFactorId, Value>
}

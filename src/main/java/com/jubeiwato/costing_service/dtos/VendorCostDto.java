package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorCostDto extends VendorDto{
    List<CostFactorValueDto> costFactorValues;

    @Builder(builderMethodName = "superBuilder")
    public VendorCostDto(Long id, String name, String address, String emailId, String contactNumber, List<CostFactorValueDto> costFactorValues) {
        super(id, name, address, emailId, contactNumber);
        this.costFactorValues = costFactorValues;
    }
}

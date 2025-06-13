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
    List<CostFactorDto> costFactorValues;

    @Builder(builderMethodName = "superBuilder")
    public VendorCostDto(Long id, String vendorName, String address, String emailId, String contactNumber, List<CostFactorDto> costFactorValues) {
        super(id, vendorName, address, emailId, contactNumber);
        this.costFactorValues = costFactorValues;
    }
}

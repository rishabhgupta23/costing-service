package com.jubeiwato.costing_service.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetVendorPartDto {
    private String partName;
    private String partNumber;

    
    public GetVendorPartDto(String partName, String partNumber) {
        this.partName = partName;
        this.partNumber = partNumber;
    }
}

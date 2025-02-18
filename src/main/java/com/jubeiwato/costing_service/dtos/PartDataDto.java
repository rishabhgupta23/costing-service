package com.jubeiwato.costing_service.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartDataDto {
    private List<PartRowDto> partsList;
    private int maxVendorCount;
}

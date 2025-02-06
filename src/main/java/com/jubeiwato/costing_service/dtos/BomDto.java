package com.jubeiwato.costing_service.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BomDto {
    Long childPartId;
    Double quantity;
}

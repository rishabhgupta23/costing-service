package com.jubeiwato.costing_service.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomDto {
     @NotNull(message = "childPartId cannot be null or blank")
    Long childPartId;
    Double quantity;
}

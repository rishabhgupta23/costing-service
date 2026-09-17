package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.entities.Part;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BomUploadDto {

    private Part parent;

    private Part child;

    private Double quantity;

}

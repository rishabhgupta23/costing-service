package com.jubeiwato.costing_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartImageUploadDto {

    @NotBlank(message = "File name must not be blank")
    private String fileName; 

    @NotBlank(message = "File data must not be blank")  
    private String fileData;   
}

package com.jubeiwato.costing_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateRequestDto {

    @NotBlank(message = "Template name must not be blank")
    private String name;

    @NotNull(message = "Part attributes must not be null")
    @NotEmpty(message = "Part attributes must not be empty")
    private List<Long> partAttributes;
}
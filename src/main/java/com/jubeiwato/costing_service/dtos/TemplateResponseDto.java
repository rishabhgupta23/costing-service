package com.jubeiwato.costing_service.dtos;

import java.util.List;

import com.jubeiwato.costing_service.entities.Template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateResponseDto {
    private Long templateId;
    private String templateName;
    private List<PartAttributeDto> partAttributes;

            public static TemplateResponseDto entityToDto(Template template) {
        return TemplateResponseDto.builder()
                .templateId(template.getTemplateId())
                .templateName(template.getTemplateName())
                .build();
    }
}


package com.jubeiwato.costing_service.dtos;
import com.jubeiwato.costing_service.entities.Template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDto {
    private Long templateId;
    private String name;

        public static TemplateDto entityToDto(Template template) {
        return TemplateDto.builder()
                .templateId(template.getTemplateId())
                .name(template.getName())
                .build();
    }
}
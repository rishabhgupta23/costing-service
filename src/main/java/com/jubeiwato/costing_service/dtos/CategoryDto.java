package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import com.jubeiwato.costing_service.entities.Category;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private String name;
    public static CategoryDto entityToDto(Category category) {
        return CategoryDto.builder()
                .name(category.getName())
                .build();
    }
}

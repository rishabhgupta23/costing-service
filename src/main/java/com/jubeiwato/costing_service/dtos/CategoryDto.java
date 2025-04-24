package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import com.jubeiwato.costing_service.entities.Category;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long categoryId;
    @NotBlank(message = "Category name cannot be empty or null")
    private String name;
    public static CategoryDto entityToDto(Category category) {
        return CategoryDto.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .build();
    }
}

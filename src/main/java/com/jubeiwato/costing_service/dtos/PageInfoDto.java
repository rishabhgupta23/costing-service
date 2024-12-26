package com.jubeiwato.costing_service.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageInfoDto {
    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalPages;
}

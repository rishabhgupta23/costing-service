package com.jubeiwato.costing_service.dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiPageResponseDto<K> {
    K data;
    PageInfoDto pageInfo;
}

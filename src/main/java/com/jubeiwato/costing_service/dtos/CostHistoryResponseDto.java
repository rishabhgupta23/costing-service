package com.jubeiwato.costing_service.dtos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostHistoryResponseDto {
    private Long partId;
    private Long vendorId;
    private List<CostHistoryDto> costHistoryList;
}

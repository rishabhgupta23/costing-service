package com.jubeiwato.costing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostHistoryDto {
    private List<CostFactorDto> costFactorList;
    private ZonedDateTime updatedDateTime;

}

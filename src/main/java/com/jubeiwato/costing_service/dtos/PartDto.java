package com.jubeiwato.costing_service.dtos;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;
import com.jubeiwato.costing_service.entities.Part;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PartDto {
    private Long partId;
    private String partName;
    private String partNumber;
    private String categoryName;
    private PartType type;
    private PartUnit unit;
    private List<CostDetails> costDetails;
    private List<BomDetails> bomDetails;

    public static PartDto enitityToDto(Part entity) {
        return PartDto.builder()
        .partId(entity.getPartId())
        .partName(entity.getPartName())
        .partNumber(entity.getPartNumber())
        .categoryName(entity.getCategoryName())
        .type(entity.getType())
        .unit(entity.getUnit())
        .build();
    }

    @Data
    @Builder
    public static class CostDetails {
        private String vendorName;
        private List<CostFactorDetails> costFactors;
    }

    @Data
    @Builder
    public static class CostFactorDetails {
        private String factorName;
        private double value;
    }

    @Data
    @Builder
    public static class BomDetails {
        private Long childPartId;
        private String childPartName;
        private String childPartNumber;
        private int quantity;
    }
}

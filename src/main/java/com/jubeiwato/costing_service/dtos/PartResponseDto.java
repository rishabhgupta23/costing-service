package com.jubeiwato.costing_service.dtos;
import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartResponseDto extends PartDto {
    private Map<VendorDto, PartCostDto> vendorCostMap;

    private List<BomResponseDto> bom;

    @Builder(builderMethodName = "superBuilder")
    public PartResponseDto(Long partId, String partName, String partNumber, String categoryName, PartType type, PartUnit unit, Map<VendorDto, PartCostDto> vendorCostMap, List<BomResponseDto> bom) {
        super(partId, partName, partNumber, categoryName, type, unit);
        this.vendorCostMap = vendorCostMap;
        this.bom=bom;
    }

    // Map Part entity to PartResponseDto
//    public static PartResponseDto entityToPartResponseDto(Part part, Map<Vendor, PartCost> vendorPartCostMap, List<Bom> bomList) {
//        return PartResponseDto.superBuilder()
//                .partId(part.getPartId())
//                .partNumber(part.getPartNumber())
//                .categoryName(part.getCategoryName())
//                .type(part.getType())
//                .unit(part.getUnit())
//                .vendorCostMap(mapVendorCostMap(vendorPartCostMap))
//                .bom(mapBomList(bomList))
//                .build();
//    }

    // Map vendorCosts to vendorCostMap
//    private static Map<VendorDto, PartCostDto> mapVendorCostMap(Map<Vendor, PartCost> vendorCosts) {
//        return vendorCosts.entrySet().stream()
//                .collect(Collectors.toMap(
//                        entry -> VendorDto.entityToDto(entry.getKey()), // Vendor to VendorDto
//                        entry -> mapPartCostToDto(entry.getValue())    // PartCost to PartCostDto
//                ));
//    }

    // Map PartCost entity to PartCostDto
//    private static PartCostDto mapPartCostToDto(PartCost cost) {
//        Map<CostFactorDto, Double> costFactors = cost.getCostFactorValues().entrySet().stream()
//                .collect(Collectors.toMap(
//                        entry -> CostFactorDto.entityToDto(entry.getKey()), // CostFactor to CostFactorDto
//                        Map.Entry::getValue                                // Use value directly
//                ));
//        return PartCostDto.builder()
//                .vendorId(cost.getVendor().getVendorId())
//                .costFactorValues(costFactors)
//                .build();
//    }

//    // Map BOM list to BOM DTOs
//    private static List<BomDto> mapBomList(List<Bom> bomList) {
//        return bomList.stream().map(BomDto::entityToDto).toList();
//    }
}

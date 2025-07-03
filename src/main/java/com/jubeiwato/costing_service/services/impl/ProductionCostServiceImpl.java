package com.jubeiwato.costing_service.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.PriceMode;
import com.jubeiwato.costing_service.dtos.CostItemDto;
import com.jubeiwato.costing_service.dtos.ProductionCostResponseDto;
import com.jubeiwato.costing_service.dtos.ProductionRequestDto;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.PartCostRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.services.ProductionCostService;

@Service
public class ProductionCostServiceImpl implements ProductionCostService {
    private final PartRepository partRepository;
    private final BomRepository bomRepository;
    private final PartCostRepository partCostRepository;

    public ProductionCostServiceImpl(
            PartRepository partRepository,
            BomRepository bomRepository,
            PartCostRepository partCostRepository) {
        this.partRepository = partRepository;
        this.bomRepository = bomRepository;
        this.partCostRepository = partCostRepository;
    }

    @Override
    public ProductionCostResponseDto calculateProductionCost(List<ProductionRequestDto> parts, String priceMode,
            Long companyId) {
        Map<Long, Double> unitPartQuantities = new java.util.HashMap<>();

        for (ProductionRequestDto requestDto : parts) {
            Long partId = requestDto.getPartId();
            Double quantity = requestDto.getQuantity();

            Part part = partRepository.findById(partId)
                    .orElseThrow(() -> new AppException(
                            ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.CHILD_PART_NOT_FOUND_TEMPLATE,
                                    partId),
                            HttpStatus.NOT_FOUND));

            if (!part.getCompany().getCompanyId().equals(companyId)) {
                throw new AppException(
                        ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.PART_NOT_FOUND_TEMPLATE, partId),
                        HttpStatus.NOT_FOUND);
            }

            if (part.getType() == PartType.UNIT) {
                unitPartQuantities.merge(partId, quantity, Double::sum);
            } else {
                accumulateUnitPartsFromMaster(partId, quantity, unitPartQuantities);
            }
        }

        List<CostItemDto> allItems = new ArrayList<>();
        double totalCost = 0.0;

        for (Map.Entry<Long, Double> entry : unitPartQuantities.entrySet()) {
            CostItemDto item = calculateUnitPart(entry.getKey(), priceMode, entry.getValue());
            allItems.add(item);
            totalCost += item.getSubTotal();
        }

        return ProductionCostResponseDto.builder()
                .items(allItems)
                .totalCost(totalCost)
                .build();
    }


    private void accumulateUnitPartsFromMaster(Long partId, Double parentQty, Map<Long, Double> unitPartQuantities) {
        List<Bom> childParts = bomRepository.findByParentPart_PartId(partId);

        for (Bom childPart : childParts) {
            Part child = childPart.getChildPart();
            Double childQty = childPart.getQuantity() * parentQty;

            if (child.getType() == PartType.UNIT) {
                unitPartQuantities.merge(child.getPartId(), childQty, Double::sum);
            } else {
                accumulateUnitPartsFromMaster(child.getPartId(), childQty, unitPartQuantities);
            }
        }
    }


    private CostItemDto calculateUnitPart(Long partId, String priceMode, Double qt) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.CHILD_PART_NOT_FOUND_TEMPLATE,
                                partId),
                        HttpStatus.NOT_FOUND));

        List<PartCost> partCostDetails = partCostRepository.getRecentByPartId(partId);

        Map<Vendor, Double> vendorCostMap = partCostDetails.stream()
                .collect(Collectors.toMap(
                        PartCost::getVendor,
                        pc -> (pc.getCostFactorList() == null) ? 0.0
                                : pc.getCostFactorList().stream().mapToDouble(PartCostCostFactor::getValue).sum()));

        Map.Entry<Vendor, Double> result = calculateMinMaxAvgCost(vendorCostMap, priceMode);

        return CostItemDto.builder()
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .quantity(qt)
                .rate(result.getValue())
                .vendorName(result.getKey().getVendorName())
                .subTotal(qt * result.getValue())
                .build();
    }

    private Map.Entry<Vendor, Double> calculateMinMaxAvgCost(Map<Vendor, Double> vendorCostMap, String priceMode) {
        switch (PriceMode.fromString(priceMode)) {
            case MIN:
                return vendorCostMap.entrySet().stream()
                        .min(Map.Entry.comparingByValue())
                        .orElse(Map.entry(new Vendor(0L, "Unknown Vendor", "", "", "", null), 0.0));

            case MAX:
                return vendorCostMap.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(Map.entry(new Vendor(0L, "Unknown Vendor", "", "", "", null), 0.0));

            case AVG:
                double avg = vendorCostMap.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .average().orElse(0.0);

                return vendorCostMap.entrySet().stream()
                        .min((entry1, entry2) -> {
                            double diff1 = Math.abs(entry1.getValue() - avg);
                            double diff2 = Math.abs(entry2.getValue() - avg);

                            if (Double.compare(diff1, diff2) == 0) {
                                return Double.compare(entry1.getValue(), entry2.getValue());
                            }
                            return Double.compare(diff1, diff2);
                        }).orElse(Map.entry(new Vendor(0L, "Unknown Vendor", "", "", "", null), 0.0));

            default:
                throw new AppException(ErrorMessageConstant.INVALID_PRICE_MODE, HttpStatus.BAD_REQUEST);
        }
    }
}

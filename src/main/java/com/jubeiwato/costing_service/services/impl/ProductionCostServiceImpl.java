package com.jubeiwato.costing_service.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.dtos.CostItemDto;
import com.jubeiwato.costing_service.dtos.ProductionCostResponseDto;
import com.jubeiwato.costing_service.dtos.ProductionPlanPartDto;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.services.CostCalcService;
import com.jubeiwato.costing_service.services.ProductionCostService;

@Service
public class ProductionCostServiceImpl implements ProductionCostService {
    private final PartRepository partRepository;
    private final BomRepository bomRepository;
    private final CostCalcService costCalcService;

    public ProductionCostServiceImpl(
            PartRepository partRepository,
            BomRepository bomRepository, CostCalcService costCalcService) {
        this.partRepository = partRepository;
        this.bomRepository = bomRepository;
        this.costCalcService = costCalcService;
    }

    @Override
    public ProductionCostResponseDto calculateProductionCost(List<ProductionPlanPartDto> parts, String priceMode,
                                                             Long companyId) {
        Map<Long, Double> unitPartQuantities = new java.util.HashMap<>();

        for (ProductionPlanPartDto requestDto : parts) {
            Long partId = requestDto.getPartId();
            Double quantity = requestDto.getQuantity();

            if (partId == null) {
                throw new AppException("Part ID cannot be null or empty", HttpStatus.BAD_REQUEST);
            }
            if (quantity == null || quantity <= 0) {
                throw new AppException("Quantity must be greater than 0 and not null", HttpStatus.BAD_REQUEST);
            }
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
            CostItemDto item = costCalcService.calculateUnitPart(entry.getKey(), priceMode, entry.getValue());
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
}

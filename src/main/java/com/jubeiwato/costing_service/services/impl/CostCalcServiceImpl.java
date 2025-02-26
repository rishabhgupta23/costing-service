package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PriceMode;
import com.jubeiwato.costing_service.dtos.CostItemDto;
import com.jubeiwato.costing_service.dtos.CostCalcResultDto;
import com.jubeiwato.costing_service.entities.*;
import com.jubeiwato.costing_service.exceptions.BadRequestException;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.PartCostRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.services.CostCalcService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class CostCalcServiceImpl implements CostCalcService {
    private final PartRepository partRepository;
    private final PartCostRepository partCostRepository;
    private final BomRepository  bomRepository;

    public CostCalcServiceImpl(PartRepository partRepository, PartCostRepository partCostRepository,BomRepository  bomRepository) {
        this.partRepository = partRepository;
        this.partCostRepository = partCostRepository;
        this.bomRepository = bomRepository;
    }

    private List<CostItemDto> calculateMasterPart(Long partId, String priceMode, Double quantity) {
        double calculatedPrice = 0.0;
        String vendorName = "";

        List<CostItemDto> masterDto = new ArrayList<>();
        List<Bom> childParts = bomRepository.findByParentPart_PartId(partId).stream().collect(Collectors.toList());

        for (Bom childPart : childParts) {

            if (childPart.getChildPart().getType() == PartType.MASTER)  {

                List<CostItemDto> childCosts = calculateMasterPart(childPart.getChildPart().getPartId(), priceMode, childPart.getQuantity());
                calculatedPrice = childCosts.stream().mapToDouble(CostItemDto::getPrice).sum();
                masterDto.add(CostItemDto.builder()
                        .partName(childPart.getChildPart().getPartName())
                        .partNumber(childPart.getChildPart().getPartNumber())
                        .quantity(quantity)
                        .price(calculatedPrice)
                        .vendorName(vendorName)
                        .rate(quantity*calculatedPrice)
                        .build());
            } else {
                CostItemDto unitDto = calculateUnitPart(childPart.getChildPart().getPartId(), priceMode, quantity);
                masterDto.add(unitDto);
            }
        }
        return masterDto;
    }

    private CostItemDto calculateUnitPart(Long partId, String  priceMode, Double qt) {
            Part part = partRepository.findById(partId)
                    .orElseThrow(() -> new BadRequestException("Child Part not found with ID: " + partId));

        List<PartCost> partCostDetails = partCostRepository.findByPartId(partId);

        Map<Vendor, Double> vendorCostMap = partCostDetails.stream()
                .collect(Collectors.toMap(
                        PartCost::getVendor,
                        pc -> (pc.getCostFactorList() == null) ? 0.0 :
                                pc.getCostFactorList().stream().mapToDouble(PartCostCostFactor::getValue).sum()
                ));
        Map.Entry<Vendor, Double> result = calculateMinMaxAvgCost(vendorCostMap, priceMode);

       return CostItemDto.builder()
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .quantity(qt)
                .price(result.getValue())
                .vendorName(result.getKey().getName())
               .rate(qt*result.getValue())
                .build();
    }

    @Override
    public CostCalcResultDto calculatePrice(Long partId, String priceMode) {
        List<CostItemDto> res;
        Double quantity = 1.0;
        Double totalCost=0.0;
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new BadRequestException("Child Part not found with ID: " + partId));

        if(part.getType() == PartType.UNIT) {
            res = new ArrayList<>();
            res.add(calculateUnitPart(partId,priceMode,quantity));
            totalCost= res.get(0).getPrice();

        } else {
            res = calculateMasterPart(partId,priceMode, quantity);
            totalCost = res.stream().mapToDouble(CostItemDto::getPrice).sum();
        }

        return CostCalcResultDto.builder()
                .costCalcDtoList(res)
                .totalCost(totalCost)
                .build();
    }


    private Map.Entry<Vendor, Double> calculateMinMaxAvgCost(Map<Vendor, Double> vendorCostMap, String priceMode) {
        switch (PriceMode.fromString(priceMode)) {
            case MIN:
                return vendorCostMap.entrySet().stream()
                        .min(Map.Entry.comparingByValue())
                        .orElse(Map.entry(new Vendor(0L, "Unknown Vendor", "", "", ""), 0.0));

            case MAX:
                return vendorCostMap.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(Map.entry(new Vendor(0L, "Unknown Vendor", "", "", ""), 0.0));

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
                        }).orElse(Map.entry(new Vendor(0L, "Unknown Vendor", "", "", ""), 0.0));

            default:
                throw new IllegalArgumentException("Invalid price mode.");
        }
    }


}

package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PriceMode;
import com.jubeiwato.costing_service.dtos.CostCalcDto;
import com.jubeiwato.costing_service.dtos.ResultCostDto;
import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
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

    private List<CostCalcDto> calculateMasterPart(Long partId, PriceMode priceMode, Double quantity) {
        double calculatedPrice = 0.0;
        String vendorName = "";

        List<CostCalcDto> masterDto = new ArrayList<>();
        List<Bom> childParts = bomRepository.findChildPartIdsByMasterPartId(partId).stream().collect(Collectors.toList());

        for (Bom childPart : childParts) {

            if (childPart.getChildPart().getType() == PartType.MASTER)  {

                List<CostCalcDto> childCosts = calculateMasterPart(childPart.getChildPart().getPartId(), priceMode, childPart.getQuantity());
                calculatedPrice = childCosts.stream().mapToDouble(CostCalcDto::getPrice).sum();
                masterDto.add(CostCalcDto.builder()
                        .partName(childPart.getChildPart().getPartName())
                        .partNumber(childPart.getChildPart().getPartNumber())
                        .quantity(quantity)
                        .price(calculatedPrice)
                        .vendorName(vendorName)
                        .rate(quantity*calculatedPrice)
                        .build());
            } else {
                CostCalcDto unitDto = calculateUnitPart(childPart.getChildPart().getPartId(), priceMode, quantity);
                masterDto.add(unitDto);
            }
        }
        return masterDto;
    }

    private CostCalcDto calculateUnitPart(Long partId, PriceMode priceMode, Double qt) {
            Part part = partRepository.findById(partId)
                    .orElseThrow(() -> new BadRequestException("Child Part not found with ID: " + partId));

        List<PartCost> partCostDetails = partCostRepository.findByPartId(partId);

        List<Map.Entry<Double, String>> vendorPricePairs = partCostDetails.stream()
                .map(pc -> Map.entry(
                        (pc.getCostFactorList() == null) ? 0.0 :
                                pc.getCostFactorList().stream().mapToDouble(PartCostCostFactor::getValue).sum(),
                        pc.getVendor().getName()
                ))
                .collect(Collectors.toList());
        Map.Entry<Double, String> result = calculateMinMaxAvgCost(vendorPricePairs, priceMode);

       return CostCalcDto.builder()
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .quantity(qt)
                .price(result.getKey())
                .vendorName(result.getValue())
               .rate(qt*result.getKey())
                .build();
    }

    @Override
    public ResultCostDto calculatePrice(Long partId, PriceMode priceMode) {
        List<CostCalcDto> res;
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
            totalCost = res.stream().mapToDouble(CostCalcDto::getPrice).sum();
        }

        return ResultCostDto.builder()
                .costCalcDtoList(res)
                .totalCost(totalCost)
                .build();
    }


    private Map.Entry<Double, String> calculateMinMaxAvgCost(List<Map.Entry<Double, String>> vendorPricePairs, PriceMode priceMode) {
        switch (priceMode) {
            case MIN:
                return vendorPricePairs.stream()
                        .min(Map.Entry.comparingByKey())
                        .orElse(Map.entry(0.0, "Unknown Vendor"));

            case MAX:
                return vendorPricePairs.stream()
                        .max(Map.Entry.comparingByKey())
                        .orElse(Map.entry(0.0, "Unknown Vendor"));

            case AVG:
                double avg = vendorPricePairs.stream()
                        .mapToDouble(Map.Entry::getKey)
                        .average().orElse(0.0);

                return vendorPricePairs.stream()
                        .min((p1, p2) -> Double.compare(Math.abs(p1.getKey() - avg), Math.abs(p2.getKey() - avg)))
                        .orElse(Map.entry(0.0, "Unknown Vendor"));

            default:
                throw new IllegalArgumentException("Invalid price mode.");
        }
    }


}

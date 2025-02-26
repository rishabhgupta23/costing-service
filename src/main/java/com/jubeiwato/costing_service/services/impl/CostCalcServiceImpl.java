package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.dtos.CostCalcDto;
import com.jubeiwato.costing_service.dtos.ResultCostDto;
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

    private List<CostCalcDto> calculateMasterPart(Long partId, String priceMode, Integer quantity) {
        double calculatedPrice = 0.0;
        String vendorName = "Unknown Vendor";

        List<CostCalcDto> masterDto = new ArrayList<>();
        List<Long> childPartIds = bomRepository.findChildPartIdsByMasterPartId(partId)
                .stream().collect(Collectors.toList());

        for (Long childPartId : childPartIds) {
            Part childPart = partRepository.findById(childPartId)
                    .orElseThrow(() -> new BadRequestException("Child Part not found with ID: " + childPartId));

            if ("MASTER".equals(childPart.getType().name())) {

                List<CostCalcDto> childCosts = calculateMasterPart(childPartId, priceMode,bomRepository.findQuantityByParentAndChild(partId,childPartId));
                calculatedPrice = childCosts.stream().mapToDouble(CostCalcDto::getPrice).sum();
                masterDto.add(CostCalcDto.builder()
                        .partName(childPart.getPartName())
                        .partNumber(childPart.getPartNumber())
                        .quantity(quantity)
                        .price(calculatedPrice)
                        .vendorName(vendorName)
                        .rate(quantity*calculatedPrice)
                        .build());
            } else {
                CostCalcDto unitDto = calculateUnitPart(childPartId, priceMode, quantity);
                masterDto.add(unitDto);
            }
        }
        return masterDto;
    }

    private CostCalcDto calculateUnitPart(Long partId, String priceMode, Integer qt) {
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
    public ResultCostDto calculatePrice(Long partId, String priceMode) {
        List<CostCalcDto> res;
        Integer quantity = 1;
        Double totalCost=0.0;
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new BadRequestException("Child Part not found with ID: " + partId));

        if("UNIT".equals(part.getType().name())){
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


    private Map.Entry<Double, String> calculateMinMaxAvgCost(List<Map.Entry<Double, String>> vendorPricePairs, String priceMode) {
        Map.Entry<Double, String> resultEntry;

        switch (priceMode.toUpperCase()) {
            case "MIN":
                resultEntry = vendorPricePairs.stream()
                        .min(Map.Entry.comparingByKey())
                        .orElse(Map.entry(0.0, "Unknown Vendor"));
                break;

            case "MAX":
                resultEntry = vendorPricePairs.stream()
                        .max(Map.Entry.comparingByKey())
                        .orElse(Map.entry(0.0, "Unknown Vendor"));
                break;

            case "AVG":
                double avg = vendorPricePairs.stream()
                        .mapToDouble(Map.Entry::getKey)
                        .average().orElse(0.0);

                resultEntry = vendorPricePairs.stream()
                        .min((p1, p2) -> Double.compare(Math.abs(p1.getKey() - avg), Math.abs(p2.getKey() - avg)))
                        .orElse(Map.entry(0.0, "Unknown Vendor"));
                break;

            default:
                throw new IllegalArgumentException("Invalid price mode. Choose from MIN, MAX, AVG.");
        }
        return resultEntry;
    }

}

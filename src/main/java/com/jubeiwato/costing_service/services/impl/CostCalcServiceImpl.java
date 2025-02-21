package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.dtos.CostCalcDto;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import com.jubeiwato.costing_service.exceptions.BadRequestException;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.PartCostRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.services.CostCalcService;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Override
    public List<CostCalcDto> calculatePrice(Long partId, String priceMode) {

            List<Long> childPartIds = bomRepository.findChildPartIdsByMasterPartId(partId)
                    .stream().distinct().collect(Collectors.toList());

        return childPartIds.stream()
                .map(childPartId -> {

                    Part childPart = partRepository.findById(childPartId)
                            .orElseThrow(() -> new BadRequestException("Child Part not found with ID: " + childPartId));

                    double calculatedPrice=0.0;
                    String vendorName = "Unknown Vendor";

                    if("MASTER".equals(childPart.getType().name())){

                        List<CostCalcDto> childCosts = calculatePrice(childPartId, priceMode);
                        calculatedPrice = childCosts.stream().mapToDouble(CostCalcDto::getPrice).sum();

                    } else {
                        List<PartCost> partCostDetails = partCostRepository.findByPartId(childPartId);

                        vendorName = partCostDetails.get(0).getVendor().getName();

                        List<Double> vendorPrices = partCostDetails.stream()
                                .map(pc -> (pc.getCostFactorList() == null) ? 0.0 :
                                        pc.getCostFactorList().stream().mapToDouble(PartCostCostFactor::getValue).sum())
                                .collect(Collectors.toList());

                        calculatedPrice = calculatePriceFromList(vendorPrices, priceMode);
                    }
                    return CostCalcDto.builder()
                            .partName(childPart.getPartName())
                            .partNumber(childPart.getPartNumber())
                            .quantity(bomRepository.findQuantityByParentAndChild(partId, childPartId))
                            .price(calculatedPrice)
                            .vendorName(vendorName)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Double calculatePriceFromList(List<Double> vendorPrices, String priceMode) {
        Double result = 0.0;
        switch (priceMode.toUpperCase()) {
            case "MIN":
                result =  vendorPrices.stream().min(Double::compareTo).orElse(0.0);
                break;

            case "MAX":
                result = vendorPrices.stream().max(Double::compareTo).orElse(0.0);
                break;
            case "AVG":
                double avg = vendorPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                if (vendorPrices.contains(avg)) {
                    result = avg;
                } else {
                    result = vendorPrices.stream()
                            .min((p1, p2) -> Double.compare(Math.abs(p1 - avg), Math.abs(p2 - avg)))
                            .orElse(0.0);
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid price mode. Choose from MIN, MAX, AVG.");
        }
        return result;
    }

}

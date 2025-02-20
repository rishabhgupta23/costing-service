package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.dtos.CostCalcDto;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import com.jubeiwato.costing_service.exceptions.BadRequestException;
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

    public CostCalcServiceImpl(PartRepository partRepository, PartCostRepository partCostRepository) {
        this.partRepository = partRepository;
        this.partCostRepository = partCostRepository;
    }

    @Override
    public CostCalcDto calculatePrice(Long partId, String priceMode) {
        System.out.println("calculatePrice called with partId: " + partId + ", priceMode: " + priceMode);
        // Fetch part details from DB
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new BadRequestException("Part not found with ID: " + partId));

        ///Fetch Part Cost
        List<PartCost> partCostDetails = partCostRepository.findByPartId(partId);

//         Extract prices
        List<Double> vendorPrices = partCostDetails.stream()
                .map(pc -> {
                    if (pc.getCostFactorList() == null) return 0.0;
                    return (pc.getCostFactorList() != null) ?
                            pc.getCostFactorList().stream().mapToDouble(PartCostCostFactor::getValue).sum() : 0.0;
                })
                .collect(Collectors.toList());

//         Compute price based on selected mode
        Double price = calculatePriceFromList(vendorPrices, priceMode);

        // Return response DTO
        return CostCalcDto.builder()
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .price(price)
                .build();
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

                // Check if the exact average exists in the list
                if (vendorPrices.contains(avg)) {
                    result = avg;
                } else {
                    // Return the nearest price to avg
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

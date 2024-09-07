package com.jubeiwato.costing_service.services.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.client.MultipartBodyBuilder.PartBuilder;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;
import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.entities.Category;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import com.jubeiwato.costing_service.exceptions.BadRequestException;
import com.jubeiwato.costing_service.repositories.CategoryRepository;
import com.jubeiwato.costing_service.repositories.CostFactorRepository;
import com.jubeiwato.costing_service.repositories.PartCostRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.repositories.VendorRepository;
import com.jubeiwato.costing_service.services.PartService;

import jakarta.validation.Valid;

@Service
public class PartServiceImpl implements PartService {

    private PartRepository partRepository;
    private CategoryRepository categoryRepository;
    private VendorRepository vendorRepository;
    private CostFactorRepository costFactorRepository;
    private PartCostRepository partCostRepository;

    public PartServiceImpl(PartRepository partRepository, CategoryRepository categoryRepository, VendorRepository vendorRepository
    , CostFactorRepository costFactorRepository, PartCostRepository partCostRepository) {
        this.partRepository = partRepository;
        this.categoryRepository = categoryRepository;
        this.vendorRepository = vendorRepository;
        this.costFactorRepository = costFactorRepository;
        this.partCostRepository = partCostRepository;
    }

    @Override
    public List<String> getPartTypes() {
        return Arrays.asList(PartType.values()).stream().map(PartType::name).toList();
    }

    @Override
    public List<String> getPartUnits() {
        return Arrays.asList(PartUnit.values()).stream().map(PartUnit::name).toList();
    }

    @Override
    public void createPart(@Valid PartRequestDto request) {
        validateCreatePartRequest(request);
        Part part = createPartEntity(request);
        partRepository.save(part);

        // save part cost details
        List<PartCost> partCostList = request.getVendorCostMap()
        .entrySet().stream().map(entry -> createPartCostEntity(part, entry.getKey(), entry.getValue().getCostFactorValues())).toList();

        partCostRepository.saveAll(partCostList);
    }

    private void validateCreatePartRequest(PartRequestDto request) {
        categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new BadRequestException("Invalid Category"));
        PartType.valueOf(request.getPartType());
        PartUnit.valueOf(request.getPartUnit());
    }

    private Part createPartEntity(PartRequestDto request) {
        return Part.builder()
        .categoryName(categoryRepository.findById(request.getCategoryId()).get().getName())
        .partName(request.getPartName())
        .partNumber(request.getPartNumber())
        // .type(request.getPartType())
        // .unit(request.getPartUnit())
        .build();
    }

    private PartCost createPartCostEntity(Part part, Long vendorId, Map<Long, Double> costFactorValues) {
        return PartCost.builder()
        .part(part)
        .vendor(vendorRepository.findById(vendorId).get())
        .costFactorList(costFactorValues.entrySet().stream().map(entry -> createPartCostCostFactor(entry.getKey(), entry.getValue())).toList())
        .build();
    }

    private PartCostCostFactor createPartCostCostFactor(Long costFactorId, Double value) {
        return PartCostCostFactor.builder()
        .costFactor(costFactorRepository.findById(costFactorId).get())
        .value(value)
        .build();
    }

}

package com.jubeiwato.costing_service.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.dtos.VendorDto;
import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.exceptions.BadRequestException;
import com.jubeiwato.costing_service.exceptions.NotFoundException;
import com.jubeiwato.costing_service.repositories.BomRepository;
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
    private BomRepository bomRepository;

    public PartServiceImpl(PartRepository partRepository, CategoryRepository categoryRepository, VendorRepository vendorRepository
    , CostFactorRepository costFactorRepository, PartCostRepository partCostRepository, BomRepository bomRepository) {
        this.partRepository = partRepository;
        this.categoryRepository = categoryRepository;
        this.vendorRepository = vendorRepository;
        this.costFactorRepository = costFactorRepository;
        this.partCostRepository = partCostRepository;
        this.bomRepository = bomRepository;
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
        if(request.getVendorCostMap() != null && !request.getVendorCostMap().isEmpty()) {
            List<PartCost> partCostList = request.getVendorCostMap()
            .entrySet().stream().map(entry -> createPartCostEntity(part, entry.getKey(), entry.getValue().getCostFactorValues())).toList();
            partCostRepository.saveAll(partCostList);
        }

        if(request.getType().equalsIgnoreCase(PartType.MASTER.name()) && request.getBom() != null && !request.getBom().isEmpty()) {
            List<Bom> bomList = request.getBom().stream().map(bomDto -> {
                Part childPart = partRepository.findById(bomDto.getChildPartId()).orElseThrow(() -> new BadRequestException("Invalid Child Part"));
                return new Bom(part, childPart, bomDto.getQuantity());
            }).toList(); 
            bomRepository.saveAll(bomList);
        }
    }

    private void validateCreatePartRequest(PartRequestDto request) { 
        if(request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new BadRequestException("Invalid Category"));
        }
        PartType.valueOf(request.getType());
        PartUnit.valueOf(request.getUnit());
    }

    private Part createPartEntity(PartRequestDto request) {
        Part part = Part.builder()
        .partName(request.getPartName())
        .partNumber(request.getPartNumber())
        .type(PartType.valueOf(request.getType()))
        .unit(PartUnit.valueOf(request.getUnit()))
        .build();
        if(request.getCategoryId() != null) {
            part.setCategoryName(categoryRepository.findById(request.getCategoryId()).get().getName());
        }
        return part;
    }

    private PartCost createPartCostEntity(Part part, Long vendorId, Map<Long, Double> costFactorValues) {
        PartCost partCost = PartCost.builder()
        .part(part)
        .vendor(vendorRepository.findById(vendorId).get())
        .costFactorList(costFactorValues.entrySet().stream().map(entry -> createPartCostCostFactor(entry.getKey(), entry.getValue())).toList())
        .build();

        for (PartCostCostFactor costFactor : partCost.getCostFactorList()) {
            costFactor.setPartCost(partCost);  // Set the partCost reference in each CostFactor
        }
        return partCost;
    }

    private PartCostCostFactor createPartCostCostFactor(Long costFactorId, Double value) {
        return PartCostCostFactor.builder()
        .costFactor(costFactorRepository.findById(costFactorId).get())
        .value(value)
        .build();
    }

    @Override
    public List<CostFactorDto> getCostFactors() {
        return costFactorRepository.findAll().stream().map(CostFactorDto::entityToDto).toList();
    }

    @Override
    public ApiPageResponseDto<PartDto> getParts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Part> partPage = partRepository.findAll(pageable);

        List<PartDto> partList = partPage.get().toList().stream().map(PartDto::enitityToDto).toList();
        PageInfoDto pageInfo = PageInfoDto.builder()
        .totalPages(partPage.getTotalPages())
        .pageNumber(page)
        .pageSize(size)
        .totalRecords(partPage.getTotalElements())
        .build();

        return ApiPageResponseDto.<PartDto>builder()
        .data(partList)
        .pageInfo(pageInfo)
        .build();
    }

    @Override
    public PartDto getPartById(Long partId) {
          Part part = this.partRepository.findById(partId)
        .orElseThrow(() -> new NotFoundException("Part does not exist"));

        // Step 2: Fetch PartCost and CostFactor details
        List<PartCost> partCosts = partCostRepository.findByPart(part);
        List<PartDto.CostDetails> costDetailsList = partCosts.stream().map(partCost -> {
            List<PartDto.CostFactorDetails> costFactors = partCost.getCostFactorList().stream()
                    .map(costFactor -> PartDto.CostFactorDetails.builder()
                            .factorName(costFactor.getCostFactor().getFactorName())
                            .value(costFactor.getValue())
                            .build())
                    .toList();

            return PartDto.CostDetails.builder()
                    .vendorName(partCost.getVendor().getVendorName())
                    .costFactors(costFactors)
                    .build();
        }).toList();

        // Step 3: Fetch BOM details if the part is of type MASTER
        List<PartDto.BomDetails> bomDetailsList = new ArrayList<>();
        if (part.getType() == PartType.MASTER) {
            List<Bom> bomList = bomRepository.findByParentPart(part);
            bomDetailsList = bomList.stream()
                    .map(bom -> PartDto.BomDetails.builder()
                            .childPartId(bom.getChildPart().getPartId())
                            .childPartName(bom.getChildPart().getPartName())
                            .childPartNumber(bom.getChildPart().getPartNumber())
                            .quantity((bom.getQuantity().intValue()))
                            .build())
                    .toList();
        }

        // Step 4: Build the response DTO
        return PartDto.builder()
                .partId(part.getPartId())
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .categoryName(part.getCategoryName())
                .type(part.getType())
                .unit(part.getUnit())
                .costDetails(costDetailsList)
                .bomDetails(bomDetailsList)
                .build();
    }

    @Override
    public PartDto updatePartById(Long partId, String partName, PartType type, PartUnit unit,
            String categoryName) {
                Part part = this.partRepository.getReferenceById(partId);
                part.setPartName(partName);
                part.setUnit(unit);
                part.setType(type);
                part.setCategoryName(categoryName);
        
                return PartDto.enitityToDto(this.partRepository.save(part));
            }

    @Override
    @Transactional
    public void deletePartById(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new BadRequestException("Part with ID " + partId + " not found."));

        boolean isChildPart = bomRepository.existsByChildPart(part);
        if (isChildPart) {
            throw new BadRequestException("Cannot Delete this part it is in BOM of other Part(s). Please remove from BOM first to delete the part.");
        }
        partRepository.delete(part);
    }

}

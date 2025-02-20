package com.jubeiwato.costing_service.services.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.entities.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.constants.PartUnit;
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
        if(request.getVendorCostList() != null && !request.getVendorCostList().isEmpty()) {
            List<PartCost> partCostList = request.getVendorCostList().stream().map(vendorCost -> createPartCostEntity(part, vendorCost)).toList();
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

    private PartCost createPartCostEntity(Part part, VendorCostDto vendorCost) {
        PartCost partCost = PartCost.builder()
        .part(part)
        .vendor(vendorRepository.findById(vendorCost.getId()).get())
        .costFactorList(vendorCost.getCostFactorValues().stream().map(cf -> createPartCostCostFactor(cf.getId(), cf.getValue())).toList())
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

          //Fetch Part Cost
        List<PartCost> partCostDetails = partCostRepository.findByPartId(partId);
          //Fetch Bom
        List<Bom> bomDetails = bomRepository.findByParentPart(part);

        //Map everything and return
        return createPartResponseDto(part, partCostDetails, bomDetails);
    }

    PartResponseDto createPartResponseDto(Part part, List<PartCost> partCost, List<Bom> bom) {
        List<BomResponseDto> bomDtoList = bom.stream().map(BomResponseDto::entityToDto).toList();
        PartResponseDto responseDto = PartResponseDto.superBuilder()
                .partId(part.getPartId())
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .unit(part.getUnit())
                .categoryName(part.getCategoryName())
                .type(part.getType())
                .bom(bomDtoList)
                .vendorCostList(createVendorCostList(partCost))
                .build();
        return  responseDto;
    }

    List<VendorCostDto> createVendorCostList(List<PartCost> partCostList) {
        return partCostList.stream()
                .collect(Collectors.groupingBy(PartCost::getVendor)) // Group by Vendor
                .entrySet().stream()
                .map(entry -> {
                    Vendor vendor = entry.getKey();
                    List<CostFactorValueDto> costFactorValues = entry.getValue().stream()
                            .flatMap(partCost -> partCost.getCostFactorList().stream()
                                    .map(partCostCostFactor -> new CostFactorValueDto(
                                            partCostCostFactor.getCostFactor().getFactorId(),
                                            partCostCostFactor.getCostFactor().getFactorName(),
                                            partCostCostFactor.getValue()
                                    ))
                            )
                            .toList();
                    return VendorCostDto.superBuilder()
                            .id(vendor.getVendorId())
                            .name(vendor.getName())
                            .address(vendor.getAddress())
                            .emailId(vendor.getEmailId())
                            .contactNumber(vendor.getContactNumber())
                            .costFactorValues(costFactorValues)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public PartDto updatePartById(Long partId, @Valid PartRequestDto request) {
        Part existingPart = partRepository.findById(partId)
                .orElseThrow(() -> new NotFoundException("Part with ID " + partId + " does not exist"));

        // Validate and update fields
        validateCreatePartRequest(request);
        existingPart.setPartName(request.getPartName());
        existingPart.setPartNumber(request.getPartNumber());
        existingPart.setType(PartType.valueOf(request.getType()));
        existingPart.setUnit(PartUnit.valueOf(request.getUnit()));

        if (request.getCategoryId() != null) {
            existingPart.setCategoryName(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BadRequestException("Invalid Category"))
                    .getName());
        }

        partRepository.save(existingPart);

        // Update vendor cost map if present
        if (request.getVendorCostList() != null && !request.getVendorCostList().isEmpty()) {
//            List<PartCost> partCosts = partCostRepository.findByPart(existingPart);
//            partCostRepository.deleteAll(partCosts); // Clear existing costs
            List<PartCost> newCosts = request.getVendorCostList().stream().map(vendorCost -> createPartCostEntity( existingPart, vendorCost)).toList();
            partCostRepository.saveAll(newCosts);
        }

        // Update BOM if type is MASTER
        if (request.getType().equalsIgnoreCase(PartType.MASTER.name())) {
            bomRepository.deleteByParentPart(existingPart); // Clear existing BOM
            if (request.getBom() != null && !request.getBom().isEmpty()) {
                List<Bom> newBom = request.getBom().stream().map(bomDto -> {
                    Part childPart = partRepository.findById(bomDto.getChildPartId())
                            .orElseThrow(() -> new BadRequestException("Invalid Child Part"));
                    return new Bom(existingPart, childPart, bomDto.getQuantity());
                }).toList();
                bomRepository.saveAll(newBom);
            }
        }

        return createPartResponseDto(existingPart, partCostRepository.findByPart(existingPart), bomRepository.findByParentPart(existingPart));
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

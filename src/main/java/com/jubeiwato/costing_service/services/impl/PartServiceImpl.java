package com.jubeiwato.costing_service.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

import com.jubeiwato.costing_service.dtos.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.entities.PartUnit;
import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.CostFactor;
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
import com.jubeiwato.costing_service.repositories.PartUnitRepository;
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
    private PartUnitRepository partUnitRepository;

    public PartServiceImpl(PartRepository partRepository, CategoryRepository categoryRepository, VendorRepository vendorRepository
    , CostFactorRepository costFactorRepository, PartCostRepository partCostRepository, BomRepository bomRepository,PartUnitRepository partUnitRepository) {
        this.partRepository = partRepository;
        this.categoryRepository = categoryRepository;
        this.vendorRepository = vendorRepository;
        this.costFactorRepository = costFactorRepository;
        this.partCostRepository = partCostRepository;
        this.bomRepository = bomRepository;
        this.partUnitRepository=partUnitRepository;
    }
    
    @Override
    public List<String> getPartTypes() {
        return Arrays.asList(PartType.values()).stream().map(PartType::name).toList();
    }

    @Override
    public  ApiPageResponseDto<List<PartUnitDto>> getPartUnits(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<PartUnit> partUnitPage = partUnitRepository.findAll(pageable);
        
        List<PartUnitDto> partUnitDtos = partUnitPage.getContent()
                .stream()
                .map(PartUnitDto::entityToDto)
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
                .pageNumber(pageNo)
                .pageSize(pageSize)
                .totalPages(partUnitPage.getTotalPages())
                .totalRecords(partUnitPage.getTotalElements())
                .build();

        return ApiPageResponseDto.<List<PartUnitDto>>builder()
                .data(partUnitDtos)
                .pageInfo(pageInfo)
                .build();
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
        if (request.getUnit() != null) {
            partUnitRepository.findByUnitName(request.getUnit())
                .orElseThrow(() -> new BadRequestException("Invalid Unit"));
        }
    }

    private Part createPartEntity(PartRequestDto request) {
        Part part = Part.builder()
        .partName(request.getPartName())
        .partNumber(request.getPartNumber())
        .type(PartType.valueOf(request.getType()))
        .unit(request.getUnit())
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
    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize);
    Page<CostFactor> costFactorPage = costFactorRepository.findAll(pageable);
    
       List<CostFactorDto> costFactorDtos = costFactorPage.getContent()
        .stream()
        .map(CostFactorDto::entityToDto)
        .toList();
    
       PageInfoDto pageInfo = PageInfoDto.builder()
        .totalPages(costFactorPage.getTotalPages())
        .pageNumber(pageNo)
        .pageSize(pageSize)
        .totalRecords(costFactorPage.getTotalElements())
        .build();
    
    return ApiPageResponseDto.<List<CostFactorDto>>builder()
        .data(costFactorDtos)
        .pageInfo(pageInfo)
        .build();
} 

    @Override
    public ApiPageResponseDto<PartDataDto> getParts(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
    Page<Object[]> partVendorList = partCostRepository.getPartVendorList(pageable);

      Integer maxVendorCount = partCostRepository.getMaxVendorCount();


       List<PartRowDto> partList = partVendorList.getContent().stream()
       .map(obj -> {
        Long partId = ((Number) obj[0]).longValue();
        String partName = (String) obj[1];
        String partNumber = (String) obj[2];
        String categoryName = (String) obj[3];
        PartType type = PartType.valueOf((String) obj[4]);
        String unit = ((String) obj[5]);
        List<String> vendorNames = obj[6] != null ? Arrays.asList(((String) obj[6]).split(",")) : List.of();

        return PartRowDto.superBuilder()
            .partId(partId)
            .partName(partName)
            .partNumber(partNumber)
            .categoryName(categoryName)
            .type(type)
            .unit(unit)
            .vendorNames(vendorNames)
            .build();
    })
             .toList();

    PartDataDto partDataDto = PartDataDto.builder()
        .partsList(partList)
        .maxVendorCount(maxVendorCount != null ? maxVendorCount : 0)
        .build();

    PageInfoDto pageInfo = PageInfoDto.builder()
        .totalPages(partVendorList.getTotalPages())
        .pageNumber(pageNo)
        .pageSize(pageSize)
        .totalRecords(partVendorList.getTotalElements())
        .build();

    return ApiPageResponseDto.<PartDataDto>builder()
        .data(partDataDto)
        .pageInfo(pageInfo)
        .build();
}

    @Override
    public PartDto getPartById(Long partId) {
          Part part = this.partRepository.findById(partId)
        .orElseThrow(() -> new NotFoundException("Part does not exist"));

          //Fetch Part Cost
        List<PartCost> partCostList = partCostRepository.findByPartId(partId);
          //Fetch Bom
        List<Bom> bomDetails = bomRepository.findByParentPart(part);

        //Map everything and return
        return createPartResponseDto(part, partCostList, bomDetails);
    }

    PartResponseDto createPartResponseDto(Part part, List<PartCost> partCostList, List<Bom> bom) {
        List<BomResponseDto> bomDtoList = bom.stream().map(BomResponseDto::entityToDto).toList();
        PartResponseDto responseDto = PartResponseDto.superBuilder()
                .partId(part.getPartId())
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .unit(part.getUnit())
                .categoryName(part.getCategoryName())
                .type(part.getType())
                .bom(bomDtoList)
                .vendorCostList(createVendorCostList(partCostList))
                .build();
        return  responseDto;
    }

    public List<VendorCostDto> createVendorCostList(List<PartCost> partCostList) {
        // Since each vendor has at most one PartCost, use toMap instead of groupingBy
        Map<Vendor, PartCost> vendorToPartCostMap = partCostList.stream()
                .collect(Collectors.toMap(PartCost::getVendor, Function.identity()));

        List<VendorCostDto> vendorCostList = new ArrayList<>();

        // Iterate through each vendor and their corresponding PartCost
        for (Map.Entry<Vendor, PartCost> entry : vendorToPartCostMap.entrySet()) {
            Vendor vendor = entry.getKey();
            PartCost partCost = entry.getValue();

            // Extract cost factor values for the vendor
            List<CostFactorValueDto> costFactorValues = partCost.getCostFactorList().stream()
                    .map(pc -> new CostFactorValueDto(
                            pc.getCostFactor().getFactorId(),
                            pc.getCostFactor().getFactorName(),
                            pc.getValue()
                    ))
                    .collect(Collectors.toList());

            // Build and add VendorCostDto to the result list
            VendorCostDto vendorCostDto = VendorCostDto.superBuilder()
                    .id(vendor.getVendorId())
                    .name(vendor.getName())
                    .address(vendor.getAddress())
                    .emailId(vendor.getEmailId())
                    .contactNumber(vendor.getContactNumber())
                    .costFactorValues(costFactorValues)
                    .build();

            vendorCostList.add(vendorCostDto);
        }

        return vendorCostList;
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
       if(request.getUnit()!=null){
        existingPart.setUnit(partUnitRepository.findByUnitName(request.getUnit())
                .orElseThrow(() -> new BadRequestException("Invalid Unit"))
                .getUnitName());
       } 

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

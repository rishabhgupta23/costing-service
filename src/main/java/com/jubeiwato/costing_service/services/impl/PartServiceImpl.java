package com.jubeiwato.costing_service.services.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.entities.PartUnit;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.CostFactorDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.dtos.PartDataDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.PartRequestDto;
import com.jubeiwato.costing_service.dtos.PartRowDto;
import com.jubeiwato.costing_service.dtos.PartUnitDto;
import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.CostFactor;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
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
    public  ApiPageResponseDto<List<PartUnitDto>> getPartUnits(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PartUnit> partUnitPage = partUnitRepository.findAll(pageable);
        
        List<PartUnitDto> partUnitDtos = partUnitPage.getContent()
                .stream()
                .map(PartUnitDto::entityToDto)
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
                .pageNumber(page)
                .pageSize(size)
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
        request.getUnit();
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
    public ApiPageResponseDto<List<CostFactorDto>> getCostFactors(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<CostFactor> costFactorPage = costFactorRepository.findAll(pageable);
    
       List<CostFactorDto> costFactorDtos = costFactorPage.getContent()
        .stream()
        .map(CostFactorDto::entityToDto)
        .toList();
    
       PageInfoDto pageInfo = PageInfoDto.builder()
        .totalPages(costFactorPage.getTotalPages())
        .pageNumber(page)
        .pageSize(size)
        .totalRecords(costFactorPage.getTotalElements())
        .build();
    
    return ApiPageResponseDto.<List<CostFactorDto>>builder()
        .data(costFactorDtos)
        .pageInfo(pageInfo)
        .build();
} 

    @Override
    public ApiPageResponseDto<PartDataDto> getParts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
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
        .pageNumber(page)
        .pageSize(size)
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

        //step 2 - fetch Part Cost
        //Step 3 -BOM

        return PartDto.enitityToDto(part);
    }

    @Override
    public PartDto updatePartById(Long partId, String partName, PartType type,String unit,
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

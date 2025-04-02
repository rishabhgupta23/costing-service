package com.jubeiwato.costing_service.services.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.services.FileGeneratorService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.DateFormat;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.FileExtension;
import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.entities.PartUnit;
import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.CostFactor;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.CategoryRepository;
import com.jubeiwato.costing_service.repositories.CostFactorRepository;
import com.jubeiwato.costing_service.repositories.PartCostRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.repositories.VendorRepository;
import com.jubeiwato.costing_service.repositories.PartUnitRepository;
import com.jubeiwato.costing_service.services.PartService;

import java.io.IOException;
import java.text.SimpleDateFormat;

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
    private FileGeneratorService excelService;

    public PartServiceImpl(PartRepository partRepository, CategoryRepository categoryRepository, VendorRepository vendorRepository
            , CostFactorRepository costFactorRepository, PartCostRepository partCostRepository, BomRepository bomRepository,PartUnitRepository partUnitRepository, FileGeneratorService excelService) {
        this.partRepository = partRepository;
        this.categoryRepository = categoryRepository;
        this.vendorRepository = vendorRepository;
        this.costFactorRepository = costFactorRepository;
        this.partCostRepository = partCostRepository;
        this.bomRepository = bomRepository;
        this.partUnitRepository=partUnitRepository;
        this.excelService=excelService;
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
                Part childPart = partRepository.findById(bomDto.getChildPartId()).orElseThrow(() -> new  AppException(ErrorMessageConstant.INVALID_CHILD_PART, HttpStatus.BAD_REQUEST));
                return new Bom(part, childPart, bomDto.getQuantity());
            }).toList();
            bomRepository.saveAll(bomList);
        }
    }

    private void validateCreatePartRequest(PartRequestDto request) {
        if(request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_CATEGORY, HttpStatus.BAD_REQUEST));
        }

        PartType.valueOf(request.getType());
        if (request.getUnit() == null || request.getUnit().isEmpty()) {
            throw new AppException(ErrorMessageConstant.UNIT_CANNOT_BE_NULL_OR_EMPTY, HttpStatus.BAD_REQUEST);
        }
        partUnitRepository.findByUnitName(request.getUnit())
                .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_UNIT, HttpStatus.BAD_REQUEST));
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
            costFactor.setPartCost(partCost);
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
                .map(costFactor -> CostFactorDto.builder()
                        .id(costFactor.getFactorId())
                        .name(costFactor.getFactorName())
                        .build())
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
    public ApiPageResponseDto<PartDataDto> getParts(PartDto filter, int pageNo, int pageSize, String sortBy, Sorting sortMode) {
        Sort sort = (sortMode == Sorting.DESC)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);


        // Apply Specification
        Specification<Part> spec = new PartSpecification(filter.getPartName(), filter.getPartNumber(), filter.getCategoryName(), filter.getType(), filter.getUnit());
        Page<Part> partPage = partRepository.findAll(spec, pageable);

        // Extract Max Vendor Count
        Integer maxVendorCount = partCostRepository.getMaxVendorCount();

        // Convert Parts to DTO
        List<PartRowDto> partList = partPage.getContent().stream()
                .map(part -> {
                    Set<String> vendorNames = part.getPartCosts().stream()
                            .map(PartCost::getVendor)
                            .map(Vendor::getName)
                            .collect(Collectors.toSet());

                    return PartRowDto.superBuilder()
                            .partId(part.getPartId())
                            .partName(part.getPartName())
                            .partNumber(part.getPartNumber())
                            .categoryName(part.getCategoryName())
                            .type(part.getType().name())
                            .unit(part.getUnit())
                            .vendorNames(new ArrayList<>(vendorNames))
                            .build();
                })
                .toList();

        PartDataDto partDataDto = PartDataDto.builder()
                .partsList(partList)
                .maxVendorCount(maxVendorCount != null ? maxVendorCount : 0)
                .build();

        PageInfoDto pageInfo = PageInfoDto.builder()
                .totalPages(partPage.getTotalPages())
                .pageNumber(pageNo)
                .pageSize(pageSize)
                .totalRecords(partPage.getTotalElements())
                .build();

        return ApiPageResponseDto.<PartDataDto>builder()
                .data(partDataDto)
                .pageInfo(pageInfo)
                .build();
    }




    @Override
    public PartDto getPartById(Long partId) {
        Part part = this.partRepository.findById(partId)
                .orElseThrow(() -> new AppException(ErrorMessageConstant.PART_DOESNOT_EXIST, HttpStatus.NOT_FOUND));

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
                .type(part.getType().toString())
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
            List<CostFactorDto> costFactorValues = partCost.getCostFactorList().stream()
                    .map(pc -> new CostFactorDto(
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
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.PART_NOT_FOUND_TEMPLATE, partId), 
                        HttpStatus.NOT_FOUND));

        // Validate and update fields
        validateCreatePartRequest(request);
        existingPart.setPartName(request.getPartName());
        existingPart.setType(PartType.valueOf(request.getType()));
        existingPart.setUnit(partUnitRepository.findByUnitName(request.getUnit())
                .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_UNIT, HttpStatus.BAD_REQUEST))
                .getUnitName());

        if (request.getCategoryId() != null) {
            existingPart.setCategoryName(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_CATEGORY , HttpStatus.BAD_REQUEST))
                    .getName());
        }

        partRepository.save(existingPart);

        // Update vendor cost map if present
        List<PartCost> partCosts = partCostRepository.findByPart(existingPart);
        List<VendorCostDto> vendorCostList = request.getVendorCostList();
        Set<Long> incomingVendorCostIds = vendorCostList.stream()
                .map(VendorCostDto::getId)
                .collect(Collectors.toSet());
        List<PartCost> toDelete = partCosts.stream()
                .filter(partCost -> !incomingVendorCostIds.contains(partCost.getVendor().getVendorId()))
                .toList();
        partCostRepository.deleteAll(toDelete);
        List<PartCost> newCosts = vendorCostList.stream().map(vendorCost -> createPartCostEntity( existingPart, vendorCost)).toList();
        partCostRepository.saveAll(newCosts);

        // Update BOM if type is MASTER
        bomRepository.deleteByParentPart(existingPart); // Clear existing BOM
        if (request.getBom() != null && !request.getBom().isEmpty()) {
            List<Bom> newBom = request.getBom().stream().map(bomDto -> {
                Part childPart = partRepository.findById(bomDto.getChildPartId())
                        .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_CHILD_PART, HttpStatus.BAD_REQUEST));
                return new Bom(existingPart, childPart, bomDto.getQuantity());
            }).toList();
            bomRepository.saveAll(newBom);
        }


        return createPartResponseDto(existingPart, partCostRepository.findByPartId(existingPart.getPartId()), bomRepository.findByParentPart(existingPart));
    }

    @Override
    @Transactional
    public void deletePartById(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new AppException(
                        ErrorMessageConstant.getFormattedMessage(ErrorMessageConstant.PART_NOT_FOUND_TEMPLATE, partId), 
                        HttpStatus.NOT_FOUND));
        boolean isChildPart = bomRepository.existsByChildPart(part);
        if (isChildPart) {
            throw new AppException(ErrorMessageConstant.RESTRICT_CHILD_PART_DELETE, HttpStatus.BAD_REQUEST);
        }
        partRepository.delete(part);
    }

    @Override
    public CostHistoryResponseDto getPartCostsByPartAndVendor(Long partId, Long vendorId) {
        List<PartCost> partCosts = partCostRepository.fetchByPartIdAndVendorId(partId, vendorId);

        List<CostHistoryDto> costHistoryList = partCosts.stream().map(partCost -> {
            List<CostFactorDto> costFactorList = partCost.getCostFactorList().stream()
                    .map(partCostFactor -> CostFactorDto.entityToDto(
                            partCostFactor.getCostFactor(),
                            partCostFactor.getValue()
                    ))
                    .toList();

            return CostHistoryDto.builder()
                    .costFactorList(costFactorList)
                    .updatedDateTime(partCost.getUpdatedDateTime())
                    .build();
        }).toList();

        return CostHistoryResponseDto.builder()
                .partId(partId)
                .vendorId(vendorId)
                .costHistoryList(costHistoryList)
                .build();
    }


    @Override
    public byte[] downloadPartsToExcel() throws IOException {

        List<Part> parts = partRepository.findAll(Sort.by(Sort.Direction.ASC, "partNumber"));

    List<String[]> partList = parts.stream()
            .map(part -> {
                Set<String> vendorNames = part.getPartCosts().stream()
                        .map(PartCost::getVendor)
                        .map(Vendor::getName)
                        .collect(Collectors.toSet());

                return new String[]{
                        part.getPartNumber(),
                        part.getPartName(),
                        part.getUnit(),
                        part.getType().toString(),
                        part.getCategoryName(),
                        String.join(", ", vendorNames)
                };
            })
            .toList();

    String[] headers = {"Part Number", "Part Name", "Measuring Unit", "Type", "Category", "Vendor Names"};
       
       return  excelService.generateSpreadsheet(partList, headers);
    

}
    @Override
    public FileResponseDto downloadBomPartListToExcel(Long parentPartId) throws IOException {

    List<Bom> bomList = bomRepository.findByParentPart_PartId(parentPartId);

    String parentPartNumber = bomList.get(0).getParentPart().getPartNumber();

    List<String[]> bomData = bomList.stream()
            .map(bom -> new String[]{
                    bom.getChildPart().getPartNumber(),
                    bom.getChildPart().getPartName(),
                    String.valueOf(bom.getQuantity())
            })
            .toList();
    String[] headers = { "Child Part Number", "Child Part Name", "Quantity" };

    byte[] fileResponse = excelService.generateSpreadsheet(bomData, headers);
    
    String base64Excel = Base64.getEncoder().encodeToString(fileResponse);

    String timestamp = new SimpleDateFormat(DateFormat.yyyyMMdd_HHmmss.getFormat()).format(new Date());
    String filename = parentPartNumber + "_Bom_" + timestamp + "." + FileExtension.SPREADSHEET.getValue();

    return FileResponseDto.builder()
            .fileData(base64Excel)
            .fileName(filename)
            .build();
  }            

}
    

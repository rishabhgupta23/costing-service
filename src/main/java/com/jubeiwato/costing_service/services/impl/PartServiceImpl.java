package com.jubeiwato.costing_service.services.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.services.FileGeneratorService;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.DateFormat;
import com.jubeiwato.costing_service.constants.DeleteFlag;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.FileExtension;
import com.jubeiwato.costing_service.constants.PartType;
import com.jubeiwato.costing_service.entities.PartUnit;
import com.jubeiwato.costing_service.entities.Bom;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.CostFactor;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.PartAttribute;
import com.jubeiwato.costing_service.entities.PartCost;
import com.jubeiwato.costing_service.entities.PartCostCostFactor;
import com.jubeiwato.costing_service.entities.PartPartAttribute;
import com.jubeiwato.costing_service.entities.PartFile;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.repositories.BomRepository;
import com.jubeiwato.costing_service.repositories.CategoryRepository;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.CostFactorRepository;
import com.jubeiwato.costing_service.repositories.PartAttributeRepository;
import com.jubeiwato.costing_service.repositories.PartCostRepository;
import com.jubeiwato.costing_service.repositories.PartPartAttributeRepository;
import com.jubeiwato.costing_service.repositories.PartFileRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.repositories.VendorRepository;
import com.jubeiwato.costing_service.repositories.PartUnitRepository;
import com.jubeiwato.costing_service.services.PartService;
import com.jubeiwato.costing_service.services.S3Service;
import com.jubeiwato.costing_service.utils.ValidationUtil;

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
        private CompanyRepository companyRepository;
        private PartPartAttributeRepository partPartAttributeRepository;
        private PartAttributeRepository partAttributeRepository;
        private final PartFileRepository partFileRepository;
        private final S3Service s3Service;

        public PartServiceImpl(PartRepository partRepository, CategoryRepository categoryRepository,PartPartAttributeRepository partPartAttributeRepository,
    PartAttributeRepository partAttributeRepository,
                        VendorRepository vendorRepository, CostFactorRepository costFactorRepository,
                        PartCostRepository partCostRepository, BomRepository bomRepository,
                        PartUnitRepository partUnitRepository,
                        FileGeneratorService excelService, CompanyRepository companyRepository,
                        PartFileRepository partFileRepository, S3Service s3Service) {
                this.partRepository = partRepository;
                this.categoryRepository = categoryRepository;
                this.vendorRepository = vendorRepository;
                this.costFactorRepository = costFactorRepository;
                this.partCostRepository = partCostRepository;
                this.bomRepository = bomRepository;
                this.partUnitRepository = partUnitRepository;
                this.excelService = excelService;
                this.companyRepository = companyRepository;
                 this.partPartAttributeRepository=partPartAttributeRepository;
        this.partAttributeRepository=partAttributeRepository;
                this.partFileRepository = partFileRepository;
                this.s3Service = s3Service;
        }

        private Part getValidatedPart(Long partId, Long companyId) {
                return partRepository.findByPartIdAndCompany_CompanyId(partId, companyId)
                                .orElseThrow(() -> new AppException(
                                                ErrorMessageConstant.PART_NOT_FOUND, HttpStatus.NOT_FOUND));

        }

        @Override
        public List<String> getPartTypes() {
                return Arrays.asList(PartType.values()).stream().map(PartType::name).toList();
        }

        @Override
        public ApiPageResponseDto<List<PartUnitDto>> getPartUnits(int pageNo, int pageSize) {
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
        @Transactional
        public PartDto createPart(@Valid PartRequestDto request, Long companyId) {
                Map<Long, Vendor> vendorMap = new HashMap<>();
                Map<Long, CostFactor> costFactorMap = new HashMap<>();

                validateCreatePartRequest(request, companyId, vendorMap, costFactorMap);
                Part part = createPartEntity(request, companyId);
                partRepository.save(part);

                if (request.getVendorCostList() != null && !request.getVendorCostList().isEmpty()) {
                        List<PartCost> partCostList = request.getVendorCostList().stream()
                                        .map(vendorCost -> createPartCostEntity(part, vendorCost, vendorMap,
                                                        costFactorMap))
                                        .toList();
                        partCostRepository.saveAll(partCostList);
                }

                if (request.getType().equalsIgnoreCase(PartType.MASTER.name()) && request.getBom() != null
                                && !request.getBom().isEmpty()) {
                        validateAndSaveBom(request, companyId, part);
                }
                savePartAttributes(request, part, false);
                return PartDto.entityToDto(part);
        }

        
        public void savePartAttributes(PartRequestDto request, Part part, boolean isUpdate) {
    if (request.getAttributeValueList() == null || request.getAttributeValueList().isEmpty()) {
        return;
    }

    Set<Long> attributeIdSet = new HashSet<>();
    for (AttributeValueDto attrValDto : request.getAttributeValueList()) {
        Long attributeId = attrValDto.getAttributeId();

        if (attributeId == null) {
            throw new AppException(ErrorMessageConstant.ATTRIBUTE_NULL, HttpStatus.BAD_REQUEST);
        }

        if (!attributeIdSet.add(attributeId)) {
            throw new AppException(ErrorMessageConstant.ATTRIBUTE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
    }

    List<PartPartAttribute> partAttributes = request.getAttributeValueList().stream().map(attrValDto -> {
        Long attributeId = attrValDto.getAttributeId();

        PartAttribute attribute;
         if (isUpdate) {
            attribute = partAttributeRepository.findById(attributeId)
                    .orElseThrow(() -> new AppException(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND, HttpStatus.NOT_FOUND));
        } else {
            attribute = partAttributeRepository
                    .findByAttributeIdAndDeleteFlag(attributeId, DeleteFlag.NEGATIVE.getValue())
                    .orElseThrow(() -> new AppException(ErrorMessageConstant.ATTRIBUTE_MARKED_DELETED, HttpStatus.BAD_REQUEST));
        }


        PartPartAttribute ppa = new PartPartAttribute();
        ppa.setPart(part);
        ppa.setAttribute(attribute);
        ppa.setAttributeValue(attrValDto.getValue());

        return ppa;
    }).toList();

    partPartAttributeRepository.saveAll(partAttributes);
}
        private void validateCreatePartRequest(PartRequestDto request, Long companyId, Map<Long, Vendor> vendorMap,
                        Map<Long, CostFactor> costFactorMap) {

                // 1. Part Number Uniqueness Check
                if (partRepository.existsByCompany_CompanyIdAndPartNumber(companyId, request.getPartNumber())) {
                        throw new AppException(
                                        ErrorMessageConstant.getFormattedMessage(
                                                        ErrorMessageConstant.PART_NUMBER_ALREADY_EXISTS_TEMPLATE,
                                                        request.getPartNumber()),
                                        HttpStatus.CONFLICT);
                }

                validateCommonPartRequest(request, companyId, vendorMap, costFactorMap);
        }

        private void validateCommonPartRequest(PartRequestDto request, Long companyId, Map<Long, Vendor> vendorMap,
                        Map<Long, CostFactor> costFactorMap) {
                if (request.getCategoryId() != null) {
                        categoryRepository.findByCategoryIdAndCompany_CompanyId(request.getCategoryId(), companyId)
                                        .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_CATEGORY,
                                                        HttpStatus.NOT_FOUND));
                }

                if (request.getUnit() == null || request.getUnit().isEmpty()) {
                        throw new AppException(ErrorMessageConstant.UNIT_CANNOT_BE_NULL_OR_EMPTY,
                                        HttpStatus.BAD_REQUEST);
                }

                partUnitRepository.findByUnitName(request.getUnit())
                                .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_UNIT,
                                                HttpStatus.BAD_REQUEST));

                if (request.getVendorCostList() != null) {
                        validateAndStoreVendors(request.getVendorCostList(), companyId, vendorMap);
                        validateAndStoreCostFactors(request.getVendorCostList(), companyId, costFactorMap);
                }

                if (request.getBom() != null && !request.getBom().isEmpty()) {
                        validateBomPartsExist(request, companyId);
                }
        }

        private void validateAndStoreVendors(List<VendorCostDto> vendorCostList, Long companyId,
                        Map<Long, Vendor> vendorMap) {

                Set<Long> vendorIds = vendorCostList.stream()
                                .map(VendorCostDto::getId)
                                .collect(Collectors.toSet());

                List<Vendor> vendors = vendorRepository.findByVendorIdInAndCompany_CompanyId(vendorIds, companyId);

                if (vendors.size() != vendorIds.size()) {
                        throw new AppException(ErrorMessageConstant.VENDOR_DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
                }

                vendors.forEach(v -> vendorMap.put(v.getVendorId(), v));
        }

        private void validateAndStoreCostFactors(List<VendorCostDto> vendorCostList, Long companyId,
                        Map<Long, CostFactor> costFactorMap) {

                Set<Long> costFactorIds = vendorCostList.stream()
                                .flatMap(vendorCost -> vendorCost.getCostFactorValues().stream())
                                .map(CostFactorDto::getId)
                                .collect(Collectors.toSet());

                List<CostFactor> costFactors = costFactorRepository.findByFactorIdInAndCompany_CompanyId(costFactorIds,
                                companyId);

                if (costFactors.size() != costFactorIds.size()) {
                        throw new AppException(ErrorMessageConstant.COST_FACTOR_DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
                }

                costFactors.forEach(cf -> costFactorMap.put(cf.getFactorId(), cf));
        }

        private List<Part> validateBomPartsExist(PartRequestDto request, Long companyId) {
                Set<Long> childPartIds = request.getBom().stream()
                                .map(BomDto::getChildPartId)
                                .collect(Collectors.toSet());

                List<Part> validChildParts = partRepository.findByPartIdInAndCompany_CompanyId(childPartIds, companyId);

                Set<Long> foundIds = validChildParts.stream()
                                .map(Part::getPartId)
                                .collect(Collectors.toSet());

                List<Long> missingIds = childPartIds.stream()
                                .filter(id -> !foundIds.contains(id))
                                .toList();

                if (!missingIds.isEmpty()) {
                        String message = "Child Part(s) not found for ID(s): " + missingIds;
                        throw new AppException(message, HttpStatus.BAD_REQUEST);
                }

                return validChildParts;
        }

        private void validateAndSaveBom(PartRequestDto request, Long companyId, Part part) {
                List<Part> validChildParts = validateBomPartsExist(request, companyId);

                Map<Long, Part> childPartMap = validChildParts.stream()
                                .collect(Collectors.toMap(Part::getPartId, Function.identity()));

                List<Bom> bomList = request.getBom().stream()
                                .map(bomDto -> new Bom(part,
                                                childPartMap.get(bomDto.getChildPartId()),
                                                bomDto.getQuantity()))
                                .toList();

                bomRepository.saveAll(bomList);
        }

        private Part createPartEntity(PartRequestDto request, Long companyId) {
                Company company = companyRepository.findById(companyId)
                                .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_COMPANY,
                                                HttpStatus.BAD_REQUEST));

                Part part = Part.builder()
                                .partName(request.getPartName())
                                .partNumber(request.getPartNumber())
                                .type(PartType.valueOf(request.getType()))
                                .unit(request.getUnit())
                                .company(company)
                                .build();
                if (request.getCategoryId() != null) {
                        part.setCategory(categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new AppException(
                                                        ErrorMessageConstant.CATEGORY_DOES_NOT_EXIST,
                                                        HttpStatus.BAD_REQUEST)));
                }
                return part;
        }

        private PartCost createPartCostEntity(Part part, VendorCostDto vendorCost, Map<Long, Vendor> vendorMap,
                        Map<Long, CostFactor> costFactorMap) {
                PartCost partCost = PartCost.builder()
                                .part(part)
                                .vendor(vendorMap.get(vendorCost.getId()))
                                .costFactorList(vendorCost.getCostFactorValues().stream()
                                                .map(cf -> createPartCostCostFactor(cf.getId(), cf.getValue(),
                                                                costFactorMap))
                                                .toList())
                                .build();

                for (PartCostCostFactor costFactor : partCost.getCostFactorList()) {
                        costFactor.setPartCost(partCost);
                }
                return partCost;
        }

        private PartCostCostFactor createPartCostCostFactor(Long costFactorId, Double value,
                        Map<Long, CostFactor> costFactorMap) {
                return PartCostCostFactor.builder()
                                .costFactor(costFactorMap.get(costFactorId))
                                .value(value != null ? value : 0.0)
                                .build();
        }

        @Override
        public ApiPageResponseDto<PartDataDto> getParts(PartDto filter, long companyId, int pageNo, int pageSize,
                        String sortBy, Sorting sortMode) {

                // Apply Specification
                if (!ValidationUtil.isValidInput(filter.getPartName()) ||
                                !ValidationUtil.isValidInput(filter.getPartNumber()) ||
                                !ValidationUtil.isValidInput(filter.getCategoryName()) ||
                                !ValidationUtil.isValidInput(filter.getUnit())) {
                        throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
                }
                Specification<Part> spec = new PartSpecification(companyId, filter.getPartName(),
                                filter.getPartNumber(),
                                filter.getCategoryName(), filter.getType(), filter.getUnit());

                if ("categoryName".equalsIgnoreCase(sortBy)) {
                        sortBy = "category.categoryName";
                }
                Sort sort = (sortMode == Sorting.DESC)
                                ? Sort.by(Sort.Order.desc(sortBy))
                                : Sort.by(Sort.Order.asc(sortBy));
                Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

                Page<Part> partPage = partRepository.findAll(spec, pageable);

                // Extract Max Vendor Count
                Integer maxVendorCount = partCostRepository.getMaxVendorCount();

                // Convert Parts to DTO
                List<PartRowDto> partList = partPage.getContent().stream()
                                .map(part -> {
                                        Set<String> vendorNames = part.getPartCosts().stream()
                                                        .map(PartCost::getVendor)
                                                        .map(Vendor::getVendorName)
                                                        .collect(Collectors.toSet());

                                        return PartRowDto.superBuilder()
                                                        .partId(part.getPartId())
                                                        .partName(part.getPartName())
                                                        .partNumber(part.getPartNumber())
                                                        .categoryName(part.getCategory() != null
                                                                        ? part.getCategory().getCategoryName()
                                                                        : null)
                                                        .type(part.getType() != null ? part.getType().name() : null)
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
        public PartDto getPartById(Long partId, Long companyId) {
                Part part = getValidatedPart(partId, companyId);

                // Fetch Part Cost
                List<PartCost> partCostList = partCostRepository.getRecentByPartId(partId);
                // Fetch Bom
                List<Bom> bomDetails = bomRepository.findByParentPart(part);


                 List<PartPartAttribute> attributeValues = partPartAttributeRepository.findByPart(part);

                // Map everything and return
                return createPartResponseDto(part, partCostList, bomDetails, attributeValues);
        }

        PartResponseDto createPartResponseDto(Part part, List<PartCost> partCostList, List<Bom> bom, List<PartPartAttribute> attributeValueList) {
                List<BomResponseDto> bomDtoList = bom.stream().map(BomResponseDto::entityToDto).toList();
                List<AttributeValueDto> attributeDtoList = attributeValueList.stream()
    .map(AttributeValueDto::entityToDto)
    .toList();
                PartDto partDto = PartDto.entityToDto(part);

                return PartResponseDto.superBuilder()
                                .partId(partDto.getPartId())
                                .partName(partDto.getPartName())
                                .partNumber(partDto.getPartNumber())
                                .unit(partDto.getUnit())
                                .categoryName(partDto.getCategoryName())
                                .type(partDto.getType())
                                .bom(bomDtoList)
                                .vendorCostList(createVendorCostList(partCostList))
                                .attributeValueList(attributeDtoList)
                                .build();

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
                                                        pc.getValue()))
                                        .collect(Collectors.toList());

                        // Build and add VendorCostDto to the result list
                        VendorCostDto vendorCostDto = VendorCostDto.superBuilder()
                                        .id(vendor.getVendorId())
                                        .vendorName(vendor.getVendorName())
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
        public PartDto updatePartById(Long partId, @Valid PartRequestDto request, Long companyId) {
                Part existingPart = getValidatedPart(partId, companyId);
                Map<Long, Vendor> vendorMap = new HashMap<>();
                Map<Long, CostFactor> costFactorMap = new HashMap<>();
                PartType oldType = existingPart.getType();
                // Validate and update fields
                validateCommonPartRequest(request, companyId, vendorMap, costFactorMap);
                existingPart.setPartName(request.getPartName());
                existingPart.setType(PartType.valueOf(request.getType()));
                existingPart.setUnit(partUnitRepository.findByUnitName(request.getUnit())
                                .orElseThrow(() -> new AppException(ErrorMessageConstant.INVALID_UNIT,
                                                HttpStatus.BAD_REQUEST))
                                .getUnitName());

                if (request.getCategoryId() != null) {
                        existingPart.setCategory(categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(
                                                        () -> new AppException(ErrorMessageConstant.INVALID_CATEGORY,
                                                                        HttpStatus.BAD_REQUEST)));
                }

                partRepository.save(existingPart);

                // Update vendor cost map if present
                updateVendorCosts(existingPart, request, vendorMap, costFactorMap);

                // Update BOM if type is MASTER
                if (oldType == PartType.MASTER && existingPart.getType() == PartType.UNIT) {
                        bomRepository.deleteByParentPart(existingPart);
                } else if (existingPart.getType() == PartType.MASTER && request.getBom() != null) {
                        bomRepository.deleteByParentPart(existingPart); // Clear existing BOM
                        List<Bom> newBom = request.getBom().stream().map(bomDto -> {
                                Part childPart = partRepository
                                                .findByPartIdAndCompany_CompanyId(bomDto.getChildPartId(), companyId)
                                                .orElseThrow(() -> new AppException(
                                                                ErrorMessageConstant.INVALID_CHILD_PART,
                                                                HttpStatus.BAD_REQUEST));
                                return new Bom(existingPart, childPart, bomDto.getQuantity());
                        }).toList();
                        bomRepository.saveAll(newBom);
                }
                partPartAttributeRepository.deleteByPart_PartId(existingPart.getPartId());
                partPartAttributeRepository.flush();
                savePartAttributes(request, existingPart, true);
                return createPartResponseDto(existingPart,
                                partCostRepository.getRecentByPartId(existingPart.getPartId()),
                                bomRepository.findByParentPart(existingPart),partPartAttributeRepository.findByPart(existingPart));
        }

        private void updateVendorCosts(Part existingPart, PartRequestDto request,
                        Map<Long, Vendor> vendorMap, Map<Long, CostFactor> costFactorMap) {

                List<PartCost> partCosts = partCostRepository.getRecentByPartId(existingPart.getPartId());

                List<VendorCostDto> vendorCostList = request.getVendorCostList() != null
                                ? request.getVendorCostList()
                                : Collections.emptyList();

                // Map existing PartCost by vendorId
                Map<Long, PartCost> existingCostMap = partCosts.stream()
                                .collect(Collectors.toMap(pc -> pc.getVendor().getVendorId(), pc -> pc));

                Set<Long> incomingVendorCostIds = vendorCostList.stream()
                                .map(VendorCostDto::getId)
                                .collect(Collectors.toSet());

                List<PartCost> toDelete = partCosts.stream()
                                .filter(partCost -> !incomingVendorCostIds.contains(partCost.getVendor().getVendorId()))
                                .toList();
                partCostRepository.deleteAll(toDelete);

                List<PartCost> toSave = new ArrayList<>();
                for (VendorCostDto dto : vendorCostList) {
                        Long vendorId = dto.getId();
                        PartCost existing = existingCostMap.get(vendorId);
                        PartCost incoming = createPartCostEntity(existingPart, dto, vendorMap, costFactorMap);

                        if (existing == null || !isPartCostEqual(existing, incoming)) {
                                toSave.add(incoming);
                        }
                }

                // Save only the new/changed costs
                if (!toSave.isEmpty()) {
                        partCostRepository.saveAll(toSave);
                }
        }

        private boolean isPartCostEqual(PartCost existing, PartCost incoming) {
                List<PartCostCostFactor> existingFactors = existing.getCostFactorList();
                List<PartCostCostFactor> incomingFactors = incoming.getCostFactorList();

                if (existingFactors.size() != incomingFactors.size())
                        return false;

                Map<Long, Double> existingMap = existingFactors.stream()
                                .collect(Collectors.toMap(
                                                f -> f.getCostFactor().getFactorId(),
                                                PartCostCostFactor::getValue));

                for (PartCostCostFactor incomingFactor : incomingFactors) {
                        Long id = incomingFactor.getCostFactor().getFactorId();
                        Double incomingValue = incomingFactor.getValue();
                        if (!Objects.equals(existingMap.get(id), incomingValue)) {
                                return false;
                        }
                }

                return true;
        }

        @Override
        @Transactional
        public void deletePartById(Long partId, Long companyId) {
                Part part = getValidatedPart(partId, companyId);

                boolean isChildPart = bomRepository.existsByChildPart(part);
                if (isChildPart) {
                        throw new AppException(ErrorMessageConstant.RESTRICT_CHILD_PART_DELETE, HttpStatus.BAD_REQUEST);
                }
                partRepository.delete(part);
        }

        @Override
        public CostHistoryResponseDto getPartCostsByPartAndVendor(Long partId, Long vendorId, Long companyId) {
                getValidatedPart(partId, companyId);
                List<PartCost> partCosts = partCostRepository.fetchByPartIdAndVendorId(partId, vendorId);

                List<CostHistoryDto> costHistoryList = partCosts.isEmpty() ? Collections.emptyList()
                                : partCosts.stream().map(partCost -> {
                                        List<CostFactorDto> costFactorList = partCost.getCostFactorList().stream()
                                                        .map(partCostFactor -> CostFactorDto.entityToDto(
                                                                        partCostFactor.getCostFactor(),
                                                                        partCostFactor.getValue()))
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
        public byte[] downloadPartsToExcel(Long companyId) throws IOException {

                List<Part> parts = partRepository.findByCompany_CompanyId(companyId,
                                Sort.by(Sort.Direction.ASC, "partNumber"));

                List<String[]> partList = parts.stream()
                                .map(part -> {
                                        Set<String> vendorNames = part.getPartCosts().stream()
                                                        .map(PartCost::getVendor)
                                                        .map(Vendor::getVendorName)
                                                        .collect(Collectors.toSet());

                                        return new String[] {
                                                        part.getPartNumber(),
                                                        part.getPartName(),
                                                        part.getUnit(),
                                                        part.getType().toString(),
                                                        part.getCategory() != null
                                                                        ? part.getCategory().getCategoryName()
                                                                        : "",
                                                        String.join(", ", vendorNames)
                                        };
                                })
                                .toList();

                String[] headers = { "Part Number", "Part Name", "Measuring Unit", "Type", "Category", "Vendor Names" };

                return excelService.generateSpreadsheet(partList, headers);

        }

        @Override
        public FileResponseDto downloadBomPartListToExcel(Long parentPartId, Long companyId) throws IOException {

                Part partInfo = partRepository.findById(parentPartId)
                                .orElseThrow(() -> new AppException(ErrorMessageConstant.PART_DOESNOT_EXIST,
                                                HttpStatus.NOT_FOUND));

                if (!partInfo.getCompany().getCompanyId().equals(companyId)) {
                        throw new AppException(ErrorMessageConstant.PART_NOT_FOUND, HttpStatus.NOT_FOUND);
                }

                List<Bom> bomList = bomRepository.findByParentPart_PartId(parentPartId);

                String parentPartNumber = partInfo.getPartNumber();

                List<String[]> bomData = bomList.isEmpty() ? new ArrayList<>()
                                : bomList.stream()
                                                .map(bom -> new String[] {
                                                                bom.getChildPart().getPartNumber(),
                                                                bom.getChildPart().getPartName(),
                                                                String.valueOf(bom.getQuantity())
                                                })
                                                .toList();
                String[] headers = { "Part Number", "Part Name", "Quantity" };

                byte[] fileResponse = excelService.generateSpreadsheet(bomData, headers);

                String base64Excel = Base64.getEncoder().encodeToString(fileResponse);

                String timestamp = new SimpleDateFormat(DateFormat.yyyyMMdd_HHmmss.getFormat()).format(new Date());
                String filename = parentPartNumber + "_Bom_" + timestamp + "." + FileExtension.SPREADSHEET.getValue();

                return FileResponseDto.builder()
                                .fileData(base64Excel)
                                .fileName(filename)
                                .build();
        }

        @Override
        public String uploadPartFile(Long partId, PartFileUploadDto partFileUploadDto, Long companyId)
                        throws DataIntegrityViolationException, IOException {
                Part part = getValidatedPart(partId, companyId);

                int imageCount = partFileRepository.countByPart(part);
                if (imageCount >= 3) {
                        throw new AppException(ErrorMessageConstant.FILES_QUANTITY_EXCEEDS_LIMIT,
                                        HttpStatus.BAD_REQUEST);
                }

                String s3Key = s3Service.uploadFile(partId, partFileUploadDto, companyId);

                PartFile partFile = PartFile.builder()
                                .part(part)
                                .s3FileKey(s3Key)
                                .build();

                try {
                        partFileRepository.save(partFile);
                } catch (DataIntegrityViolationException ex) {
                        // Catch unique constraint violation (like duplicate file for part)
                        throw new AppException(ErrorMessageConstant.FILE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST); // note
                                                                                                                  // that
                                                                                                                  // we
                                                                                                                  // will
                                                                                                                  // change
                                                                                                                  // this
                                                                                                                  // logic
                                                                                                                  // later
                }

                return "File uploaded successfully";
        }

        @Override
        public FileResponseDto downloadFileFromS3(String s3FileKey, Long companyId) {
                Optional<PartFile> partFileOpt = partFileRepository.findByS3FileKey(s3FileKey);
                if (partFileOpt.isEmpty()
                                || !partFileOpt.get().getPart().getCompany().getCompanyId().equals(companyId)) {
                        throw new AppException(ErrorMessageConstant.FILE_NOT_FOUND_OR_UNAUTHORIZED,
                                        HttpStatus.NOT_FOUND);
                }

                byte[] fileBytes = s3Service.downloadFile(s3FileKey);
                String base64File = Base64.getEncoder().encodeToString(fileBytes);

                String fileName = s3FileKey.substring(s3FileKey.lastIndexOf("/") + 1);

                return FileResponseDto.builder()
                                .fileData(base64File)
                                .fileName(fileName)
                                .build();
        }

        @Override
        public List<String> getPartFileUrls(Long partId, Long companyId) {
                Part part = getValidatedPart(partId, companyId);
                List<PartFile> files = partFileRepository.findByPart(part);
                return files.stream()
                                .map(PartFile::getS3FileKey)
                                .collect(Collectors.toList());
        }

}

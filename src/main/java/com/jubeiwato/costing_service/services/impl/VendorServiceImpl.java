package com.jubeiwato.costing_service.services.impl;

import java.io.IOException;
import java.util.List;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.services.FileGeneratorService;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PageInfoDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.VendorDto;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.repositories.VendorRepository;
import com.jubeiwato.costing_service.services.VendorService;
import com.jubeiwato.costing_service.services.VendorSpecification;
import com.jubeiwato.costing_service.utils.ValidationUtil;

@Service
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final PartRepository partRepository;

        private final CompanyRepository companyRepository;

    private final FileGeneratorService excelService;

        public VendorServiceImpl(VendorRepository vendorRepository, PartRepository partRepository,
                        CompanyRepository companyRepository, FileGeneratorService excelService) {
                this.vendorRepository = vendorRepository;
                this.partRepository = partRepository;
                this.companyRepository = companyRepository;
                this.excelService = excelService;
        }

        private void validateDuplicateVendor(Long companyId, String name) {
                boolean exists = vendorRepository.existsByCompanyCompanyIdAndName(companyId, name);
                if (exists) {
                        throw new AppException(ErrorMessageConstant.VENDOR_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
                }
        }

        private Vendor getAndValidateVendor(Long id, Long companyId) {
                return vendorRepository.findByVendorIdAndCompany_CompanyId(id, companyId)
                                .orElseThrow(() -> new AppException(ErrorMessageConstant.VENDOR_DOES_NOT_EXIST,HttpStatus.NOT_FOUND));

        }

        @Override
        public void createVendor(Long companyId, String name, String emailId, String contactNumber, String address) {

                if (name == null || name.trim().isEmpty()) {
                        throw new AppException(ErrorMessageConstant.VENDOR_NAME_NOT_NULL, HttpStatus.BAD_REQUEST);
                }

                if (!ValidationUtil.isValidInput(name)) {
                        throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
                }
                Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException("Company not found", HttpStatus.BAD_REQUEST));
                validateDuplicateVendor(companyId, name);
        Vendor vendor = Vendor.builder()

        .company(company)
        .name(name)
        .emailId(emailId)
        .contactNumber(contactNumber)
        .address(address)
        .build();

        vendorRepository.save(vendor);
    }

    @Override
    public ApiPageResponseDto<List<VendorDto>> getVendorList(Long companyId, String name, String address, String emailId, String contactNumber, int pageNo, int pageSize, String sortColumn ,Sorting sortMode) {
                Sort.Direction direction = (sortMode == Sorting.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortColumn);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
       if (!ValidationUtil.isValidInput(name) ||
    !ValidationUtil.isValidInput(address) ||
    !ValidationUtil.isValidInput(emailId) ||
    !ValidationUtil.isValidInput(contactNumber)) {
    throw new AppException(ErrorMessageConstant.INVALID_INPUT, HttpStatus.BAD_REQUEST);
}

Specification<Vendor> spec = new VendorSpecification(companyId, name, address, emailId, contactNumber);

        Page<Vendor> vendorPage = vendorRepository.findAll(spec, pageable);

        List<VendorDto> vendorDtos = vendorPage.getContent().stream()
                .map(VendorDto::entityToDto)
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
             .totalPages(vendorPage.getTotalPages())
             .pageNumber(pageNo)
             .pageSize(pageSize)
             .totalRecords(vendorPage.getTotalElements())
                .build();

        return ApiPageResponseDto.<List<VendorDto>>builder()
                .data(vendorDtos)
                .pageInfo(pageInfo)
                .build();
    }

    @Override
    public VendorDto getVendorById(Long id, Long companyId) {

                Vendor vendor = getAndValidateVendor(id, companyId);
        return VendorDto.entityToDto(vendor);
    }

    @Override
    public VendorDto updateVendorById(Long id, String name, String emailId, String contactNumber, String address,Long companyId) {
                Vendor vendor = getAndValidateVendor(id, companyId);
                boolean exists = vendorRepository.existsByCompanyCompanyIdAndNameAndVendorIdNot(companyId, name, id);
                if (exists) {
                        throw new AppException(ErrorMessageConstant.VENDOR_ALREADY_EXISTS, HttpStatus.NOT_FOUND);
                }
        vendor.setName(name);
        vendor.setEmailId(emailId);
        vendor.setContactNumber(contactNumber);
        vendor.setAddress(address);

        return VendorDto.entityToDto(this.vendorRepository.save(vendor)); }

    @Transactional
    @Override
    public void deleteVendorById(Long id, Long companyId) {
                Vendor vendor = getAndValidateVendor(id, companyId);
        vendorRepository.deleteById(id);
    }

    @Override
    public ApiPageResponseDto<List<PartDto>> getVendorParts(Long vendorId, int pageNo, int pageSize,Long companyId) {
                Vendor vendor = getAndValidateVendor(vendorId, companyId);
        PageRequest pageable = PageRequest.of(pageNo, pageSize);
        Page<Part> partVendorList = partRepository.getVendorParts(vendorId, pageable);

        List<PartDto> partDtos = partVendorList.getContent().stream()
            .map(PartDto::enitityToDto)
            .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
            .totalPages(partVendorList.getTotalPages())
            .pageNumber(pageNo)
            .pageSize(pageSize)
            .totalRecords(partVendorList.getTotalElements())
            .build();

            return ApiPageResponseDto.<List<PartDto>>builder()
                                .data(partDtos)
                                .pageInfo(pageInfo)
                                .build();

    }

        @Override
        public byte[] downloadVendorExcel(Long companyId) throws IOException {

                List<Vendor> vendors = vendorRepository.findByCompanyCompanyId(companyId);
                String[] headers = { "Name", "Email", "Contact Number", "Address" };

                List<String[]> data = vendors.stream()

                                .map(vendor -> new String[] {
                                                vendor.getName(),
                                                vendor.getEmailId(),
                                                vendor.getContactNumber(),
                                                vendor.getAddress()
                                })
                                .toList();

                return excelService.generateSpreadsheet(data, headers);
        }

}
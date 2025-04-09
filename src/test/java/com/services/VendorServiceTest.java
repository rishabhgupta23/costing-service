package com.services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.ApiPageResponseDto;
import com.jubeiwato.costing_service.dtos.PartDto;
import com.jubeiwato.costing_service.dtos.VendorDto;
import com.jubeiwato.costing_service.entities.Company;
import com.jubeiwato.costing_service.entities.Part;
import com.jubeiwato.costing_service.entities.Vendor;
import com.jubeiwato.costing_service.repositories.CompanyRepository;
import com.jubeiwato.costing_service.repositories.PartRepository;
import com.jubeiwato.costing_service.repositories.VendorRepository;
import com.jubeiwato.costing_service.services.FileGeneratorService;
import com.jubeiwato.costing_service.services.impl.VendorServiceImpl;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

        @Mock
        private CompanyRepository companyRepository;

        @Mock
        private VendorRepository vendorRepository;

        @InjectMocks
        private VendorServiceImpl vendorService;

        @Mock

        private FileGeneratorService excelService;

        @Mock
        private PartRepository partRepository;

        @Test
        void testCreateVendor_CompanyNotFound() {
                Long companyId = 99L;

                when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

                AppException exception = assertThrows(AppException.class, () -> vendorService.createVendor(companyId,
                                "Test", "test@mail.com", "9999999999", "Nowhere"));

                assertEquals("Company not found", exception.getMessage());
        }

        @Test
        void testGetVendorList_WithSortingDESC() {
                Long companyId = 1L;
                String name = "VendorX";
                String address = "Kolkata";
                String emailId = "vendorx@mail.com";
                String contactNumber = "9876543210";
                int pageNo = 0;
                int pageSize = 10;
                String sortColumn = "name";
                Sorting sortMode = Sorting.DESC;

                Vendor vendor = Vendor.builder()
                                .name(name)
                                .address(address)
                                .emailId(emailId)
                                .contactNumber(contactNumber)
                                .company(Company.builder().companyId(companyId).build())
                                .build();

                Page<Vendor> vendorPage = new PageImpl<>(List.of(vendor));

                when(vendorRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(vendorPage);

                ApiPageResponseDto<List<VendorDto>> response = vendorService.getVendorList(
                                companyId, name, address, emailId, contactNumber,
                                pageNo, pageSize, sortColumn, sortMode);

                assertNotNull(response);
                assertEquals(1, response.getData().size());

                VendorDto vendorDto = response.getData().get(0);
                assertEquals(name, vendorDto.getName());
                assertEquals(address, vendorDto.getAddress());
                assertEquals(emailId, vendorDto.getEmailId());
                assertEquals(contactNumber, vendorDto.getContactNumber());

                assertEquals(1, response.getPageInfo().getTotalRecords());
                assertEquals(pageNo, response.getPageInfo().getPageNumber());
                assertEquals(pageSize, response.getPageInfo().getPageSize());

                // Additional check if Sort.Direction was correctly interpreted (indirectly)
                ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
                verify(vendorRepository).findAll(any(Specification.class), pageableCaptor.capture());
                Sort sort = pageableCaptor.getValue().getSort();
                assertTrue(sort.getOrderFor(sortColumn).isDescending());
        }

        @Test
        void getVendorListTest() {
                Vendor vendor = Vendor.builder()
                                .name("VendorX")
                                .address("Kolkata")
                                .emailId("vendorx@mail.com")
                                .contactNumber("9876543210")
                                .build();

                Page<Vendor> vendorPage = new PageImpl<>(List.of(vendor));

                when(vendorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(vendorPage);

                ApiPageResponseDto<List<VendorDto>> response = vendorService.getVendorList(1L, "VendorX", "Kolkata",
                                "vendorx@mail.com", "9876543210",
                                0, 10, "name", Sorting.ASC);

                assertNotNull(response);
                assertNotNull(response.getData());
                assertEquals(1, response.getData().size());

                VendorDto vendorDto = response.getData().get(0);

                assertEquals("VendorX", vendorDto.getName());
                assertEquals("Kolkata", vendorDto.getAddress());
                assertEquals("vendorx@mail.com", vendorDto.getEmailId());
                assertEquals("9876543210", vendorDto.getContactNumber());

                assertNotNull(response.getPageInfo());
                assertEquals(1, response.getPageInfo().getTotalRecords());
                assertEquals(0, response.getPageInfo().getPageNumber());
                assertEquals(10, response.getPageInfo().getPageSize());

                verify(vendorRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));

        }

        @Test
        void testGetVendorById_VendorNotFound() {
                Long vendorId = 42L, companyId = 1L;

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.empty());

                AppException exception = assertThrows(AppException.class,
                                () -> vendorService.getVendorById(vendorId, companyId));

                assertEquals("Vendor does not exist", exception.getMessage());
        }

        @Test
        void testGetVendorById() {
                Long vendorId = 1L, companyId = 10L;

                Vendor vendor = Vendor.builder()
                                .vendorId(vendorId)
                                .name("VendorX")
                                .emailId("vendorx@mail.com")
                                .company(Company.builder().companyId(companyId).build())
                                .build();

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

                VendorDto dto = vendorService.getVendorById(vendorId, companyId);

                assertEquals("VendorX", dto.getName());
                assertEquals("vendorx@mail.com", dto.getEmailId());
        }

        @Test
        void testGetVendorById_CompanyMismatch() {
                Long vendorId = 1L, companyId = 100L;

                Vendor vendor = Vendor.builder()
                                .vendorId(vendorId)
                                .name("VendorY")
                                .company(Company.builder().companyId(999L).build())
                                .build();

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

                AppException exception = assertThrows(AppException.class,
                                () -> vendorService.getVendorById(vendorId, companyId));

                assertEquals("Vendor does not exist", exception.getMessage());
        }

        @Test
        void testUpdateVendorById() {
                Long vendorId = 1L, companyId = 10L;

                Company company = Company.builder().companyId(companyId).build();

                Vendor existingVendor = Vendor.builder()
                                .vendorId(vendorId)
                                .name("Old Name")
                                .emailId("old@mail.com")
                                .contactNumber("0000000000")
                                .address("Old Address")
                                .company(company)
                                .build();

                Vendor updatedVendor = Vendor.builder()
                                .vendorId(vendorId)
                                .name("New Name")
                                .emailId("new@mail.com")
                                .contactNumber("1234567890")
                                .address("New Address")
                                .company(company)
                                .build();

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(existingVendor));
                when(vendorRepository.save(any(Vendor.class))).thenReturn(updatedVendor);

                VendorDto dto = vendorService.updateVendorById(
                                vendorId, "New Name", "new@mail.com", "1234567890", "New Address", companyId);

                assertEquals("New Name", dto.getName());
                assertEquals("new@mail.com", dto.getEmailId());
                assertEquals("1234567890", dto.getContactNumber());
                assertEquals("New Address", dto.getAddress());
        }

        @Test
        void testUpdateVendorById_VendorNotFound() {
                Long vendorId = 1L, companyId = 10L;

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.empty());

                assertThrows(AppException.class, () -> {
                        vendorService.getVendorById(vendorId, companyId);
                });
        }

        @Test
        void testGetVendorById_NotFound() {
                Long vendorId = 999L;
                Long companyId = 1L;

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.empty());

                assertThrows(AppException.class, () -> {
                        vendorService.getVendorById(vendorId, companyId);
                });

        }

        @Test
        void testDeleteVendorById() {
                Long vendorId = 1L, companyId = 10L;

                Vendor vendor = Vendor.builder()
                                .vendorId(vendorId)
                                .name("VendorX")
                                .emailId("vendorx@mail.com")
                                .company(Company.builder().companyId(companyId).build())
                                .build();

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

                vendorService.deleteVendorById(vendorId, companyId);

                verify(vendorRepository, times(1)).deleteById(vendorId);
                verify(vendorRepository).findById(vendorId);
        }

        @Test
        void testGetVendorParts() {
                Long vendorId = 1L, companyId = 10L;
                int pageNo = 0, pageSize = 2;

                Vendor vendor = Vendor.builder()
                                .vendorId(vendorId)
                                .company(Company.builder().companyId(companyId).build())
                                .build();

                Part part1 = Part.builder()
                                .partId(101L)
                                .partName("Part A")
                                .partNumber("PA-123")
                                .company(vendor.getCompany())
                                .build();

                Part part2 = Part.builder()
                                .partId(102L)
                                .partName("Part B")
                                .partNumber("PB-456")
                                .company(vendor.getCompany())
                                .build();

                Page<Part> partPage = new PageImpl<>(List.of(part1, part2));

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
                when(partRepository.getVendorParts(eq(vendorId), any(Pageable.class))).thenReturn(partPage);

                ApiPageResponseDto<List<PartDto>> response = vendorService.getVendorParts(vendorId, pageNo, pageSize,
                                companyId);

                assertNotNull(response);
                assertEquals(2, response.getData().size());
                assertEquals("Part A", response.getData().get(0).getPartName());

                assertEquals(1, response.getPageInfo().getTotalPages()); // Since 2 items only
                verify(partRepository).getVendorParts(eq(vendorId), any(Pageable.class));
        }

        @Test
        void testGetVendorParts_VendorNotFound() {
                Long vendorId = 1L;
                Long companyId = 10L;
                int pageNo = 0, pageSize = 2;

                when(vendorRepository.findById(vendorId)).thenReturn(Optional.empty());

                assertThrows(AppException.class,
                                () -> vendorService.getVendorParts(vendorId, pageNo, pageSize, companyId));

                verify(vendorRepository).findById(vendorId);
                verify(partRepository, never()).getVendorParts(anyLong(), any(Pageable.class));
        }

        @Test
        void testDownloadVendorExcel() throws IOException {
                Long companyId = 1L;

                List<Vendor> vendors = List.of(
                                Vendor.builder().name("Vendor A").emailId("a@mail.com")
                                                .contactNumber("1234567890").address("Address 1").build(),
                                Vendor.builder().name("Vendor B").emailId("b@mail.com")
                                                .contactNumber("0987654321").address("Address 2").build());

                when(vendorRepository.findByCompanyCompanyId(companyId)).thenReturn(vendors);
                byte[] dummyExcelData = "excel".getBytes();
                when(excelService.generateSpreadsheet(any(), any())).thenReturn(dummyExcelData);

                byte[] result = vendorService.downloadVendorExcel(companyId);

                assertNotNull(result);
                assertArrayEquals(dummyExcelData, result);

                ArgumentCaptor<List<String[]>> dataCaptor = ArgumentCaptor.forClass(List.class);
                ArgumentCaptor<String[]> headerCaptor = ArgumentCaptor.forClass(String[].class);

                verify(excelService).generateSpreadsheet(dataCaptor.capture(), headerCaptor.capture());

                assertArrayEquals(
                                new String[] { "Name", "Email", "Contact Number", "Address" },
                                headerCaptor.getValue());
                assertEquals(2, dataCaptor.getValue().size());
        }

        @Test
        void testDownloadVendorExcel_NoVendors() throws IOException {
                Long companyId = 2L;

                when(vendorRepository.findByCompanyCompanyId(companyId)).thenReturn(Collections.emptyList());
                when(excelService.generateSpreadsheet(eq(Collections.emptyList()), any())).thenReturn(new byte[0]);

                byte[] result = vendorService.downloadVendorExcel(companyId);

                assertNotNull(result);
                assertEquals(0, result.length);

                verify(vendorRepository).findByCompanyCompanyId(companyId);
                verify(excelService).generateSpreadsheet(eq(Collections.emptyList()),
                                eq(new String[] { "Name", "Email", "Contact Number", "Address" }));
        }

}

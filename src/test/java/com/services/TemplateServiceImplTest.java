package com.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jubeiwato.costing_service.authentication.config.AppException;
import com.jubeiwato.costing_service.constants.ErrorMessageConstant;
import com.jubeiwato.costing_service.constants.Sorting;
import com.jubeiwato.costing_service.dtos.*;
import com.jubeiwato.costing_service.entities.*;
import com.jubeiwato.costing_service.repositories.*;
import com.jubeiwato.costing_service.services.impl.TemplateServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

class TemplateServiceImplTest {

        @InjectMocks
        private TemplateServiceImpl templateService;

        @Mock
        private TemplateRepository templateRepository;
        @Mock
        private PartAttributeRepository partAttributeRepository;
        @Mock
        private CompanyRepository companyRepository;
        @Mock
        private TemplatePartAttributeRepository templatePartAttributeRepository;

        private Company testCompany;
        private Template testTemplate;
        private List<PartAttribute> testPartAttributes;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                testCompany = Company.builder()
                                .companyId(1L)
                                .companyName("TestCo")
                                .build();

                testTemplate = Template.builder()
                                .templateId(100L)
                                .templateName("Test Template")
                                .company(testCompany)
                                .build();

                PartAttribute attr1 = PartAttribute.builder().attributeId(10L).attributeName("Attr1")
                                .company(testCompany).build();
                PartAttribute attr2 = PartAttribute.builder().attributeId(20L).attributeName("Attr2")
                                .company(testCompany).build();
                testPartAttributes = List.of(attr1, attr2);
        }

        @Test
        void createTemplate_Success() {
                TemplateRequestDto dto = TemplateRequestDto.builder()
                                .templateName("New Template")
                                .partAttributes(List.of(10L, 20L))
                                .build();

                when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
                when(templateRepository.existsByTemplateNameIgnoreCaseAndCompany("New Template", testCompany))
                                .thenReturn(false);
                when(templateRepository.save(any())).thenAnswer(invocation -> {
                        Template t = invocation.getArgument(0);
                        t.setTemplateId(101L);
                        return t;
                });
                when(partAttributeRepository.findByAttributeIdInAndCompany(anyList(), eq(testCompany)))
                                .thenReturn(testPartAttributes);
                when(templatePartAttributeRepository.saveAll(anyList())).thenReturn(null);

                assertDoesNotThrow(() -> templateService.createTemplate(dto, 1L));

                verify(templateRepository).save(any());
                verify(templatePartAttributeRepository).saveAll(anyList());
        }

        @Test
        void createTemplate_Throws_WhenTemplateNameNullOrEmpty() {
                TemplateRequestDto dto1 = TemplateRequestDto.builder()
                                .templateName(null)
                                .partAttributes(List.of(10L))
                                .build();
                TemplateRequestDto dto2 = TemplateRequestDto.builder()
                                .templateName("   ")
                                .partAttributes(List.of(10L))
                                .build();

                AppException ex1 = assertThrows(AppException.class, () -> templateService.createTemplate(dto1, 1L));
                assertEquals(ErrorMessageConstant.TEMPLATE_NULL, ex1.getMessage());

                AppException ex2 = assertThrows(AppException.class, () -> templateService.createTemplate(dto2, 1L));
                assertEquals(ErrorMessageConstant.TEMPLATE_NULL, ex2.getMessage());
        }

        @Test
        void createTemplate_Throws_WhenNoAttributesSelected() {
                TemplateRequestDto dto1 = TemplateRequestDto.builder()
                                .templateName("Valid Name")
                                .partAttributes(Collections.emptyList())
                                .build();

                TemplateRequestDto dto2 = TemplateRequestDto.builder()
                                .templateName("Valid Name")
                                .partAttributes(null)
                                .build();

                AppException ex1 = assertThrows(AppException.class, () -> templateService.createTemplate(dto1, 1L));
                assertEquals(ErrorMessageConstant.ATTRIBUTE_NOT_SELECTED, ex1.getMessage());

                AppException ex2 = assertThrows(AppException.class, () -> templateService.createTemplate(dto2, 1L));
                assertEquals(ErrorMessageConstant.ATTRIBUTE_NOT_SELECTED, ex2.getMessage());
        }

        @Test
        void createTemplate_Throws_WhenCompanyInvalid() {
                when(companyRepository.findById(1L)).thenReturn(Optional.empty());

                TemplateRequestDto dto = TemplateRequestDto.builder()
                                .templateName("Valid Name")
                                .partAttributes(List.of(10L))
                                .build();

                AppException ex = assertThrows(AppException.class, () -> templateService.createTemplate(dto, 1L));
                assertEquals(ErrorMessageConstant.INVALID_COMPANY, ex.getMessage());
        }

        @Test
        void createTemplate_Throws_WhenTemplateNameExists() {
                when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
                when(templateRepository.existsByTemplateNameIgnoreCaseAndCompany("Exists", testCompany))
                                .thenReturn(true);

                TemplateRequestDto dto = TemplateRequestDto.builder()
                                .templateName("Exists")
                                .partAttributes(List.of(10L))
                                .build();

                AppException ex = assertThrows(AppException.class, () -> templateService.createTemplate(dto, 1L));
                assertEquals(ErrorMessageConstant.TEMPLATE_ALREADY_EXISTS, ex.getMessage());
        }

        @Test
        void createTemplate_Throws_WhenAttributesNotFound() {
                when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
                when(templateRepository.existsByTemplateNameIgnoreCaseAndCompany("Valid", testCompany))
                                .thenReturn(false);

                when(partAttributeRepository.findByAttributeIdInAndCompany(anyList(), eq(testCompany)))
                                .thenReturn(Collections.emptyList());

                TemplateRequestDto dto = TemplateRequestDto.builder()
                                .templateName("Valid")
                                .partAttributes(List.of(10L, 20L))
                                .build();

                AppException ex = assertThrows(AppException.class, () -> templateService.createTemplate(dto, 1L));
                assertTrue(ex.getMessage().contains(ErrorMessageConstant.ATTRIBUTE_NOT_FOUND));
        }

        @Test
        void getAllTemplates_Success() {
                Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "templateName"));

                List<Template> templates = List.of(testTemplate);

                Page<Template> page = new PageImpl<>(templates, pageable, templates.size());

                when(templateRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

                ApiPageResponseDto<List<TemplateResponseDto>> response = templateService.getAllTemplates(1L, "Test", 0,
                                2, "templateName", Sorting.ASC);

                assertNotNull(response);
                assertEquals(1, response.getData().size());
                assertEquals(testTemplate.getTemplateName(), response.getData().get(0).getTemplateName());
                assertEquals(1, response.getPageInfo().getTotalRecords());
        }

        @Test
        void getAllTemplates_Throws_InvalidInput() {
                AppException ex = assertThrows(AppException.class,
                                () -> templateService.getAllTemplates(1L, "%", 0, 1, "templateName", Sorting.DESC));
                assertEquals(ErrorMessageConstant.INVALID_INPUT, ex.getMessage());
        }

        @Test
        void getTemplateById_Success() {
                when(templateRepository.findByTemplateIdAndCompany_CompanyId(100L, 1L))
                                .thenReturn(Optional.of(testTemplate));

                Template_PartAttribute rel1 = Template_PartAttribute.builder()
                                .template(testTemplate)
                                .partAttribute(testPartAttributes.get(0))
                                .build();
                Template_PartAttribute rel2 = Template_PartAttribute.builder()
                                .template(testTemplate)
                                .partAttribute(testPartAttributes.get(1))
                                .build();

                when(templatePartAttributeRepository.findByTemplate(testTemplate))
                                .thenReturn(List.of(rel1, rel2));

                TemplateResponseDto response = templateService.getTemplateById(100L, 1L);

                assertNotNull(response);
                assertEquals(testTemplate.getTemplateName(), response.getTemplateName());
                assertEquals(2, response.getPartAttributes().size());
        }

        @Test
        void getTemplateById_Throws_NotFound() {
                when(templateRepository.findByTemplateIdAndCompany_CompanyId(100L, 1L)).thenReturn(Optional.empty());

                AppException ex = assertThrows(AppException.class,
                                () -> templateService.getTemplateById(100L, 1L));
                assertEquals(ErrorMessageConstant.TEMPLATE_NOT_FOUND, ex.getMessage());
        }

        @Test
        void updateTemplate_Success_ChangeName() {
                TemplateRequestDto dto = TemplateRequestDto.builder()
                                .templateName("New Name")
                                .partAttributes(List.of(10L, 20L))
                                .build();

                when(templateRepository.findByTemplateIdAndCompany_CompanyId(100L, 1L))
                                .thenReturn(Optional.of(testTemplate));
                when(templateRepository.existsByTemplateNameIgnoreCaseAndCompany("New Name", testCompany))
                                .thenReturn(false);
                when(partAttributeRepository.findByAttributeIdInAndCompany(anyList(), eq(testCompany)))
                                .thenReturn(testPartAttributes);
                doNothing().when(templatePartAttributeRepository).deleteByTemplate(testTemplate);
                when(templatePartAttributeRepository.saveAll(anyList())).thenReturn(null);
                when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                TemplateResponseDto updated = templateService.updateTemplate(100L, dto, 1L);

                assertEquals("New Name", updated.getTemplateName());
                assertEquals(2, updated.getPartAttributes().size());
        }

        @Test
        void updateTemplate_Success_SameName() {
                TemplateRequestDto dto = TemplateRequestDto.builder()
                                .templateName(testTemplate.getTemplateName())
                                .partAttributes(List.of(10L))
                                .build();

                when(templateRepository.findByTemplateIdAndCompany_CompanyId(100L, 1L))
                                .thenReturn(Optional.of(testTemplate));
                when(partAttributeRepository.findByAttributeIdInAndCompany(anyList(), eq(testCompany)))
                                .thenReturn(testPartAttributes);
                doNothing().when(templatePartAttributeRepository).deleteByTemplate(testTemplate);
                when(templatePartAttributeRepository.saveAll(anyList())).thenReturn(null);
                when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                TemplateResponseDto updated = templateService.updateTemplate(100L, dto, 1L);

                assertEquals(testTemplate.getTemplateName(), updated.getTemplateName());
                assertEquals(2, updated.getPartAttributes().size());
        }

        @Test
        void updateTemplate_Throws_TemplateNotFound() {
                when(templateRepository.findByTemplateIdAndCompany_CompanyId(100L, 1L)).thenReturn(Optional.empty());

                TemplateRequestDto dto = TemplateRequestDto.builder()
                                .templateName("Any Name")
                                .partAttributes(List.of(10L))
                                .build();

                AppException ex = assertThrows(AppException.class, () -> templateService.updateTemplate(100L, dto, 1L));
                assertEquals(ErrorMessageConstant.TEMPLATE_NOT_FOUND, ex.getMessage());
        }

        @Test
        void updateTemplate_Throws_TemplateNameExists() {
                TemplateRequestDto dto = TemplateRequestDto.builder()
                                .templateName("Exists")
                                .partAttributes(List.of(10L))
                                .build();

                when(templateRepository.findByTemplateIdAndCompany_CompanyId(100L, 1L))
                                .thenReturn(Optional.of(testTemplate));
                when(templateRepository.existsByTemplateNameIgnoreCaseAndCompany("Exists", testCompany))
                                .thenReturn(true);

                AppException ex = assertThrows(AppException.class, () -> templateService.updateTemplate(100L, dto, 1L));
                assertEquals(ErrorMessageConstant.TEMPLATE_ALREADY_EXISTS, ex.getMessage());
        }

        @Test
        void deleteTemplate_Success() {
                when(templateRepository.findByTemplateIdAndCompany_CompanyId(100L, 1L))
                                .thenReturn(Optional.of(testTemplate));
                doNothing().when(templatePartAttributeRepository).deleteByTemplate(testTemplate);
                doNothing().when(templateRepository).delete(testTemplate);

                assertDoesNotThrow(() -> templateService.deleteTemplate(100L, 1L));

                verify(templatePartAttributeRepository).deleteByTemplate(testTemplate);
                verify(templateRepository).delete(testTemplate);
        }

        @Test
        void deleteTemplate_Throws_TemplateNotFound() {
                when(templateRepository.findByTemplateIdAndCompany_CompanyId(100L, 1L)).thenReturn(Optional.empty());

                AppException ex = assertThrows(AppException.class, () -> templateService.deleteTemplate(100L, 1L));
                assertEquals(ErrorMessageConstant.TEMPLATE_NOT_FOUND, ex.getMessage());
        }
}

package com.jubeiwato.costing_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Template;
import com.jubeiwato.costing_service.entities.Template_PartAttribute;

public interface TemplatePartAttributeRepository  extends JpaRepository<Template_PartAttribute, Long> {

    List<Template_PartAttribute> findByTemplateAndPartAttribute_DeleteFlag(Template template, int deleteFlag);
    void deleteByTemplate(Template template);



}

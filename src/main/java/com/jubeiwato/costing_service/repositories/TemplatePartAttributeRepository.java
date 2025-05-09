package com.jubeiwato.costing_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jubeiwato.costing_service.entities.Template_PartAttribute;

public interface TemplatePartAttributeRepository  extends JpaRepository<Template_PartAttribute, Long> {

}

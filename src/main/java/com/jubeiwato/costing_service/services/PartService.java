package com.jubeiwato.costing_service.services;

import java.util.List;

import com.jubeiwato.costing_service.dtos.PartRequestDto;

public interface PartService {

    public List<String> getPartTypes();

    public List<String> getPartUnits();

    public void createPart(PartRequestDto request);
}

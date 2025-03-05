package com.jubeiwato.costing_service.services;

import java.util.List;

public interface FileGeneratorService {
    byte[] generateSpreadsheet(List<String[]> data, String[] headers);
}
package com.jubeiwato.costing_service.services;

import java.io.IOException;
import java.util.List;

public interface FileGeneratorService {
    byte[] generateSpreadsheet(List<String[]> data, String[] headers) throws IOException;
}

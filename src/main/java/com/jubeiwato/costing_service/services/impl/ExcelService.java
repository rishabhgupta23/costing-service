package com.jubeiwato.costing_service.services.impl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {
    public byte[] generateExcel(List<String[]> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vendors");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Email", "Address", "Contact Number"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Fill data starting from row 1
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            String[] rowData = data.get(i);
            for (int j = 0; j < rowData.length; j++) {
                row.createCell(j).setCellValue(rowData[j]);
            }
        }

        // Auto-size columns for better visibility
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}

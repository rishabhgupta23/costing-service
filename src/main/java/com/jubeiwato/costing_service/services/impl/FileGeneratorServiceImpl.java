package com.jubeiwato.costing_service.services.impl;

import com.jubeiwato.costing_service.services.FileGeneratorService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class FileGeneratorServiceImpl implements FileGeneratorService {
    public byte[] generateSpreadsheet(List<String[]> data, String[] headers) {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Sheet sheet = workbook.createSheet("Sheet1");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] rowData = data.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    row.createCell(j).setCellValue(rowData[j]);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        } finally {
            try {
                workbook.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
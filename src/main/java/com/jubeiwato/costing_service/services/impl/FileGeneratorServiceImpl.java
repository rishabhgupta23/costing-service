
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

    @Override
    public byte[] generateSpreadsheet(List<String[]> data, String[] headers)
            throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Sheet1");

            // Header style
            CellStyle headerStyle = createCellStyle(
            workbook,
            true,
            IndexedColors.WHITE.getIndex(),
            IndexedColors.DARK_BLUE.getIndex(),
            HorizontalAlignment.CENTER,
            null
        );

            // Body style
            CellStyle bodyStyle = createCellStyle(
            workbook,
            false,
            IndexedColors.AUTOMATIC.getIndex(),
            null,
            HorizontalAlignment.GENERAL,
            null
        );

            // Numeric style
            CellStyle numberStyle = createCellStyle(
            workbook,
            false,
            IndexedColors.AUTOMATIC.getIndex(),
            null,
            HorizontalAlignment.GENERAL,
            "#,##0.00"
            );

            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            double totalCost = 0.0;

            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] rowData = data.get(i);

                for (int j = 0; j < headers.length; j++) {
                    Cell cell = row.createCell(j);
                    String value = j < rowData.length ? rowData[j] : "";

                    // Parse numeric values for Excel calculations.
                    if (isNumericColumn(headers[j]) && isNumeric(value)) {
                        double numericValue = Double.parseDouble(value);
                        cell.setCellValue(numericValue);
                        cell.setCellStyle(numberStyle);

                        if ("Sub Total".equalsIgnoreCase(headers[j])) {
                            totalCost += numericValue;
                        }
                    } else {
                        cell.setCellValue(value == null ? "" : value);
                        cell.setCellStyle(bodyStyle);
                    }
                }
            }

            addTotalCost(
                sheet,
                workbook,
                headers,
                data.size(),
                totalCost,
                bodyStyle,
                numberStyle
            );

            // Column sizing
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);

                // Prevent excessively wide columns.
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(width + 512, 40 * 256));
            }

            // Keep headers visible while scrolling.
            sheet.createFreezePane(0, 1);

            // Enable filtering on the header and data rows.
            if (!data.isEmpty()) {
                sheet.setAutoFilter(
                        new org.apache.poi.ss.util.CellRangeAddress(
                                0, data.size(), 0, headers.length - 1
                        )
                );
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void setBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private boolean isNumericColumn(String header) {
        return "Quantity".equalsIgnoreCase(header)
                || "Rate".equalsIgnoreCase(header)
                || "Sub Total".equalsIgnoreCase(header);
    }

    private boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int findHeader(String[] headers, String target) {
        for (int i = 0; i < headers.length; i++) {
            if (target.equalsIgnoreCase(headers[i])) {
                return i;
            }
        }

        return -1;
    }
    private CellStyle createCellStyle(
        Workbook workbook,
        boolean bold,
        short fontColor,
        Short backgroundColor,
        HorizontalAlignment alignment,
        String numberFormat) {

        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(bold);
        font.setColor(fontColor);
        font.setFontHeightInPoints((short) 11);

        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(alignment);

        if (backgroundColor != null) {
            style.setFillForegroundColor(backgroundColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        setBorders(style);

        if (numberFormat != null) {
            style.setDataFormat(
                    workbook.createDataFormat().getFormat(numberFormat)
            );
        }

        return style;
    }

    private void addTotalCost(
    Sheet sheet,
    Workbook workbook,
    String[] headers,
    int dataSize,
    double totalCost,
    CellStyle bodyStyle,
    CellStyle numberStyle) {

        int subtotalColumn = findHeader(headers, "Sub Total");

        if (subtotalColumn < 0) {
            return;
        }

        Row totalRow = sheet.createRow(dataSize + 1);

        Font totalFont = workbook.createFont();
        totalFont.setBold(true);

        CellStyle totalLabelStyle = workbook.createCellStyle();
        totalLabelStyle.cloneStyleFrom(bodyStyle);
        totalLabelStyle.setFont(totalFont);
        totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);

        CellStyle totalValueStyle = workbook.createCellStyle();
        totalValueStyle.cloneStyleFrom(numberStyle);
        totalValueStyle.setFont(totalFont);

        Cell labelCell = totalRow.createCell(
                Math.max(0, subtotalColumn - 1)
        );
        labelCell.setCellValue("Total Cost:");
        labelCell.setCellStyle(totalLabelStyle);

        Cell totalCell = totalRow.createCell(subtotalColumn);
        totalCell.setCellValue(totalCost);
        totalCell.setCellStyle(totalValueStyle);
    }
}
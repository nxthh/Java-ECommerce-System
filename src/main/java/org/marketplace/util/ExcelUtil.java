package org.marketplace.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.marketplace.model.TableData;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    public static TableData read(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path);
             Workbook workbook = new XSSFWorkbook(input)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IOException("Excel workbook has no sheets.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null || headerRow.getLastCellNum() <= 0) {
                throw new IOException("Excel header is missing.");
            }

            int columns = headerRow.getLastCellNum();
            String[] headers = new String[columns];

            for (int i = 0; i < columns; i++) {
                headers[i] = text(headerRow.getCell(i));
            }

            List<String[]> rows = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row excelRow = sheet.getRow(i);

                if (excelRow == null) {
                    continue;
                }

                if (excelRow.getLastCellNum() > columns) {
                    throw new IOException(
                            "Extra columns on Excel row " + (i + 1)
                    );
                }

                String[] row = new String[columns];
                boolean empty = true;

                for (int j = 0; j < columns; j++) {
                    row[j] = text(excelRow.getCell(j));

                    if (!row[j].isBlank()) {
                        empty = false;
                    }
                }

                if (!empty) {
                    rows.add(row);
                }
            }

            return new TableData(headers, rows);
        }
    }

    public static void write(Path path, TableData data) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            Row header = sheet.createRow(0);

            for (int i = 0; i < data.getHeader().length; i++) {
                header.createCell(i).setCellValue(data.getHeader()[i]);
            }

            int rowNumber = 1;

            for (String[] values : data.getRow()) {
                Row row = sheet.createRow(rowNumber++);

                for (int i = 0; i < values.length; i++) {
                    row.createCell(i).setCellValue(values[i]);
                }
            }

            for (int i = 0; i < data.getHeader().length; i++) {
                sheet.setColumnWidth(i, 22 * 256);
            }

            try (OutputStream output = Files.newOutputStream(path)) {
                workbook.write(output);
            }
        }
    }

    private static String text(Cell cell) throws IOException {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue())
                        .stripTrailingZeros().toPlainString();

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case BLANK:
                return "";

            default:
                throw new IOException(
                        "Use plain values, not formulas or error cells."
                );
        }
    }
}
package org.marketplace.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.marketplace.exception.DataAccessException;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Excel (.xlsx) import / export helper backed by Apache POI.
 */
public class ExcelUtil {

    private ExcelUtil() {}

    /**
     * Writes rows to an Excel .xlsx file.
     *
     * @param file    target file path.
     * @param headers column headers (written as the first row).
     * @param rows    data rows.
     */
    public static void write(String file, List<String> headers, List<List<String>> rows) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet  sheet    = wb.createSheet("Products");
            Row    headerRow = sheet.createRow(0);

            // Bold header style
            CellStyle boldStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            boldStyle.setFont(font);

            for (int c = 0; c < headers.size(); c++) {
                Cell cell = headerRow.createCell(c);
                cell.setCellValue(headers.get(c));
                cell.setCellStyle(boldStyle);
            }

            for (int r = 0; r < rows.size(); r++) {
                Row dataRow = sheet.createRow(r + 1);
                List<String> row = rows.get(r);
                for (int c = 0; c < row.size(); c++) {
                    dataRow.createCell(c).setCellValue(row.get(c));
                }
            }

            // Auto-size columns
            for (int c = 0; c < headers.size(); c++) {
                sheet.autoSizeColumn(c);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to write Excel file: " + file, e);
        }
    }

    /**
     * Reads all records from an Excel .xlsx file (skipping the header row).
     *
     * @param file source file path.
     * @return list of rows; each row is a list of string cell values.
     */
    public static List<List<String>> read(String file) {
        List<List<String>> result = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> rowIter = sheet.iterator();
            if (rowIter.hasNext()) rowIter.next(); // skip header

            while (rowIter.hasNext()) {
                Row row = rowIter.next();
                List<String> values = new ArrayList<>();
                for (Cell cell : row) {
                    values.add(getCellString(cell));
                }
                result.add(values);
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to read Excel file: " + file, e);
        }
        return result;
    }

    // -----------------------------------------------------------------------

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }
}

package org.marketplace.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.marketplace.exception.DataAccessException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV import / export helper backed by Apache Commons CSV.
 */
public class CsvUtil {

    private CsvUtil() {}

    /**
     * Writes rows to a CSV file.
     *
     * @param file    target file path.
     * @param headers column headers.
     * @param rows    data rows (each row is a list of values).
     */
    public static void write(String file, List<String> headers, List<List<String>> rows) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(new String[0]))
                .build();
        try (BufferedWriter bw = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
             CSVPrinter printer = new CSVPrinter(bw, format)) {

            for (List<String> row : rows) {
                printer.printRecord(row);
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to write CSV: " + file, e);
        }
    }

    /**
     * Reads all records from a CSV file (skipping the header row).
     *
     * @param file source file path.
     * @return list of rows; each row is a list of string values in column order.
     */
    public static List<List<String>> read(String file) {
        List<List<String>> result = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();
        try (BufferedReader br = new BufferedReader(
                     new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(br, format)) {

            for (CSVRecord record : parser) {
                List<String> row = new ArrayList<>();
                for (String value : record) {
                    row.add(value);
                }
                result.add(row);
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to read CSV: " + file, e);
        }
        return result;
    }
}

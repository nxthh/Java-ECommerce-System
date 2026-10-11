package org.marketplace.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.marketplace.model.TableData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvUtil {

    public static TableData read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {

            List<CSVRecord> records = parser.getRecords();

            if (records.isEmpty()) {
                throw new IOException("CSV file is empty.");
            }

            CSVRecord first = records.get(0);
            String[] headers = new String[first.size()];

            for (int i = 0; i < headers.length; i++) {
                headers[i] = first.get(i);
            }

            // Remove a possible UTF-8 BOM.
            headers[0] = headers[0].replace("\uFEFF", "");

            List<String[]> rows = new ArrayList<>();

            for (int i = 1; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                String[] row = new String[record.size()];

                for (int j = 0; j < row.length; j++) {
                    row[j] = unprotect(record.get(j));
                }

                rows.add(row);
            }

            return new TableData(headers, rows);
        }
    }

    public static void write(Path path, TableData data) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);

        try (Writer writer = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(
                     writer, CSVFormat.DEFAULT)) {

            printer.printRecord(Arrays.asList(data.getHeader()));

            for (String[] row : data.getRow()) {
                List<String> values = new ArrayList<>();

                for (String value : row) {
                    values.add(protect(value));
                }

                printer.printRecord(values);
            }
        }
    }

    private static boolean needsProtection(String value) {
        String trimmed = value.stripLeading();

        if (trimmed.isEmpty()) {
            return false;
        }

        char first = trimmed.charAt(0);

        return first == '=' || first == '+' || first == '-'
                || first == '@' || first == '\'';
    }

    private static String protect(String value) {
        if (value == null) {
            return "";
        }

        if (needsProtection(value)) {
            return "'" + value;
        }

        return value;
    }

    private static String unprotect(String value) {
        if (value.startsWith("'")
                && needsProtection(value.substring(1))) {
            return value.substring(1);
        }

        return value;
    }
}
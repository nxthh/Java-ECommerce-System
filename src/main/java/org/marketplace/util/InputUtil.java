package org.marketplace.util;

import org.marketplace.exception.DataAccessException;
import org.marketplace.model.TableData;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.io.Console;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

public class InputUtil {

    private static final Scanner scanner = new Scanner(System.in);

    private InputUtil() {
    }

    public static String text(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static String required(String prompt, int maxLength) {
        while (true) {
            String value = text(prompt);

            if (value.isEmpty()) {
                message("This field is required.");
                continue;
            }

            if (value.length() > maxLength) {
                message(
                        "Please enter at most "
                                + maxLength
                                + " characters."
                );
                continue;
            }

            return value;
        }
    }

    public static String password(String prompt) {
        Console console = System.console();

        if (console != null) {
            char[] characters = console.readPassword("%s", prompt);

            if (characters == null) {
                throw new IllegalStateException("Input was closed.");
            }

            try {
                return new String(characters);
            } finally {
                Arrays.fill(characters, '\0');
            }
        }

        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static int number(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(text(prompt));
            } catch (NumberFormatException exception) {
                message("Please enter a valid whole number.");
            }
        }
    }

    public static long id(String prompt) {
        while (true) {
            try {
                long value = Long.parseLong(text(prompt));

                if (value > 0) {
                    return value;
                }

                message("ID must be greater than zero.");
            } catch (NumberFormatException exception) {
                message("Please enter a valid ID.");
            }
        }
    }

    public static BigDecimal money(String prompt) {
        while (true) {
            try {
                return new BigDecimal(text(prompt));
            } catch (NumberFormatException exception) {
                message("Please enter a valid amount, such as 12.50.");
            }
        }
    }

    public static boolean yes(String prompt) {
        String question = prompt.trim();

        question = question.replaceFirst(
                "(?i)\\s*\\(y/n\\)\\s*:?\\s*$",
                ""
        );

        question = question.replaceFirst(":\\s*$", "").trim();

        while (true) {
            String answer = text(question + " (y/n): ");

            if (answer.equalsIgnoreCase("y")) {
                return true;
            }

            if (answer.equalsIgnoreCase("n")) {
                return false;
            }

            message("Please enter y or n.");
        }
    }

    public static void message(String message) {
        System.out.println();
        System.out.println(message);
    }

    public static int menu(
            String title,
            String zeroLabel,
            String... options
    ) {
        System.out.println();
        System.out.println("=== " + title.toUpperCase() + " ===");

        Table table = new Table(
                2,
                BorderStyle.UNICODE_BOX_DOUBLE_BORDER,ShownBorders.ALL
        );

        table.setColumnWidth(0, 8, 8);
        table.setColumnWidth(1, 30, 45);

        CellStyle center = new CellStyle(
                CellStyle.HorizontalAlign.CENTER
        );

        table.addCell("OPTION", center);
        table.addCell("ACTION", center);

        for (int i = 0; i < options.length; i++) {
            table.addCell(String.valueOf(i + 1));
            table.addCell(clean(options[i]));
        }

        table.addCell("0");
        table.addCell(clean(zeroLabel));

        System.out.println(table.render());

        while (true) {
            int choice = number("Choose: ");

            if (choice >= 0 && choice <= options.length) {
                return choice;
            }

            message(
                    "Please choose an option from 0 to "
                            + options.length
                            + "."
            );
        }
    }

    public static void table(TableData data) {
        if (data == null
                || data.getHeader() == null
                || data.getHeader().length == 0) {

            message("No data to display.");
            return;
        }

        String[] headers = data.getHeader();

        Table table = new Table(
                headers.length,
                BorderStyle.UNICODE_BOX_DOUBLE_BORDER,ShownBorders.ALL
        );

        for (int i = 0; i < headers.length; i++) {
            table.setColumnWidth(i, 15, 35);
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.CENTER);

        for (String header : headers) {
            table.addCell(
                    clean(header)
                            .replace('_', ' ')
                            .toUpperCase(),center
            );
        }

        int count = 0;

        if (data.getRow() != null) {
            for (String[] row : data.getRow()) {
                for (int i = 0; i < headers.length; i++) {
                    String value = "";

                    if (row != null && i < row.length) {
                        value = clean(row[i]);
                    }

                    if ("is_active".equalsIgnoreCase(headers[i])) {
                        if ("t".equalsIgnoreCase(value)
                                || "true".equalsIgnoreCase(value)) {
                            value = "Active";
                        } else if ("f".equalsIgnoreCase(value)
                                || "false".equalsIgnoreCase(value)) {
                            value = "Inactive";
                        }
                    }

                    table.addCell(value);
                }

                count++;
            }
        }

        System.out.println();
        System.out.println(table.render());

        if (count == 0) {
            System.out.println("No records found.");
        } else {
            System.out.println("Total: " + count + " record(s)");
        }
    }

    public static void error(Exception exception) {
        if (exception instanceof SQLException
                || exception instanceof DataAccessException) {

            Throwable cause = exception;

            while (cause != null) {
                if (cause instanceof SQLException) {
                    SQLException sqlException = (SQLException) cause;

                    printDatabaseError(sqlException);
                    return;
                }

                cause = cause.getCause();
            }

            message("Could not access the database.");
            return;
        }

        String details = exception.getMessage();

        if (details == null || details.isBlank()) {
            message("An unexpected error occurred.");
        } else {
            message(details);
        }
    }

    private static void printDatabaseError(SQLException exception) {
        String state = exception.getSQLState();

        if (state == null) {
            message("A database operation failed.");
            return;
        }

        switch (state) {
            case "23505":
                message("This record already exists.");
                break;

            case "23503":
                message(
                        "A referenced record does not exist, "
                                + "or this record is still in use."
                );
                break;

            case "23514":
                message("A value does not meet the database requirements.");
                break;

            case "23502":
                message("A required value is missing.");
                break;

            case "42P01":
                message("A required table is missing.");
                break;

            default:
                message("Database error. SQL state: " + state);
                break;
        }
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ');
    }
}
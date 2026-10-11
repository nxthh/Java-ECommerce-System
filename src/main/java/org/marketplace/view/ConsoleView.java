package org.marketplace.view;

import org.marketplace.model.TableData;
import org.nocrala.tools.texttablefmt.Table;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;

public class ConsoleView {

    private final Scanner scanner = new Scanner(System.in);

    public String text(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String password(String prompt) {
        if (System.console() != null) {
            char[] value = System.console().readPassword("%s", prompt);

            if (value == null) {
                throw new IllegalStateException("Input closed.");
            }

            String result = new String(value);
            Arrays.fill(value, '\0');
            return result;
        }

        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int number(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(text(prompt));
            } catch (NumberFormatException exception) {
                message("Enter a whole number.");
            }
        }
    }

    public long id(String prompt) {
        while (true) {
            try {
                long value = Long.parseLong(text(prompt));

                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }

            message("Enter a positive ID.");
        }
    }

    public BigDecimal money(String prompt) {
        while (true) {
            try {
                return new BigDecimal(text(prompt));
            } catch (NumberFormatException exception) {
                message("Enter a valid amount.");
            }
        }
    }

    public boolean yes(String prompt) {
        while (true) {
            String answer = text(prompt + " (y/n): ");

            if ("y".equalsIgnoreCase(answer)) {
                return true;
            }

            if ("n".equalsIgnoreCase(answer)) {
                return false;
            }

            message("Enter y or n.");
        }
    }

    public void message(String message) {
        System.out.println("\n" + message);
    }

    public int menu(String title, String... options) {
        System.out.println("\n=== " + title + " ===");

        Table table = new Table(2);
        table.addCell("OPTION");
        table.addCell("ACTION");

        for (int i = 0; i < options.length; i++) {
            table.addCell(String.valueOf(i + 1));
            table.addCell(options[i]);
        }

        table.addCell("0");
        table.addCell("Back / Exit");

        System.out.println(table.render());
        return number("Choose: ");
    }

    public void table(TableData data) {
        Table table = new Table(data.getHeader().length);

        for (String header : data.getHeader()) {
            table.addCell(header.toUpperCase().replace('_', ' '));
        }

        for (String[] row : data.getRow()) {
            for (String value : row) {
                table.addCell(value
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replace("\t", " "));
            }
        }

        System.out.println(table.render());

        if (data.getRow().isEmpty()) {
            System.out.println("No records found.");
        }
    }
}
package org.marketplace.view;

import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;
import org.marketplace.model.TableData;

/**
 * Shared console-rendering utilities used by all view classes.
 * Wraps the text-table-formatter library to draw ASCII tables.
 */
public class ConsoleView {

    private ConsoleView() {}

    /**
     * Renders a {@link TableData} as a bordered ASCII table.
     *
     * @param data the headers and rows to display.
     */
    public static void printTable(TableData data) {
        if (data.getHeaders() == null || data.getHeaders().isEmpty()) {
            System.out.println("  (no data)");
            return;
        }

        Table table = new Table(data.getHeaders().size(),
                BorderStyle.UNICODE_BOX_WIDE,
                ShownBorders.ALL);

        CellStyle centeredHeader = new CellStyle(CellStyle.HorizontalAlign.CENTER);
        for (String header : data.getHeaders()) {
            table.addCell(header, centeredHeader);
        }

        if (data.getRows().isEmpty()) {
            table.addCell("  (no records)", new CellStyle(CellStyle.HorizontalAlign.LEFT),
                    data.getHeaders().size());
        } else {
            for (java.util.List<String> row : data.getRows()) {
                for (String cell : row) {
                    table.addCell(cell == null ? "" : cell);
                }
            }
        }

        System.out.println(table.render());
    }

    /** Prints a decorated section header. */
    public static void printHeader(String title) {
        String line = "═".repeat(title.length() + 4);
        System.out.println("\n╔" + line + "╗");
        System.out.println("║  " + title + "  ║");
        System.out.println("╚" + line + "╝");
    }

    /** Prints a success message in a standard format. */
    public static void printSuccess(String msg) {
        System.out.println("  ✔ " + msg);
    }

    /** Prints an error message in a standard format. */
    public static void printError(String msg) {
        System.out.println("  ✘ " + msg);
    }

    /** Prints an info line. */
    public static void printInfo(String msg) {
        System.out.println("  ℹ " + msg);
    }
}

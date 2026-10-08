package org.marketplace.util;

import java.util.Scanner;

/**
 * Console input helper used by all view classes.
 * Wraps a single {@link Scanner} over {@link System#in} so the stream is
 * never closed prematurely by a try-with-resources block.
 */
public class InputUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    private InputUtil() {}

    /** Prints the prompt and reads the next line, trimmed. */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    /**
     * Prints the prompt and reads an integer.
     * Keeps asking until a valid integer is entered.
     */
    public static int readInt(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid whole number.");
            }
        }
    }

    /**
     * Prints the prompt and reads a double.
     * Keeps asking until a valid number is entered.
     */
    public static double readDouble(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }

    /**
     * Reads a menu choice between {@code min} and {@code max} (inclusive).
     * Keeps asking on invalid input.
     */
    public static int readChoice(String prompt, int min, int max) {
        while (true) {
            int choice = readInt(prompt);
            if (choice >= min && choice <= max) {
                return choice;
            }
            System.out.printf("  Please enter a number between %d and %d.%n", min, max);
        }
    }

    /**
     * Reads a yes/no answer.
     *
     * @return {@code true} when the user types 'y' or 'yes' (case-insensitive).
     */
    public static boolean readYesNo(String prompt) {
        String ans = readLine(prompt + " [y/n]: ").toLowerCase();
        return ans.equals("y") || ans.equals("yes");
    }
}

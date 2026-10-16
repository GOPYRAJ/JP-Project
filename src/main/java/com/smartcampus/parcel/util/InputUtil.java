package com.smartcampus.parcel.util;

import java.util.Scanner;

/**
 * InputUtil provides safe, user-friendly console input handling.
 * It prevents NoSuchElementException and InputMismatchException crashes
 * through validated input loops.
 */
public final class InputUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    private InputUtil() {
        // Prevent instantiation
    }

    /**
     * Reads a non-empty string from the user.
     *
     * @param prompt message displayed to the user
     * @return trimmed non-empty string
     */
    public static String readString(String prompt) {
        return readString(prompt, false);
    }

    /**
     * Reads a string from the user with option to allow empty input.
     *
     * @param prompt message displayed to the user
     * @param allowEmpty true if blank input is accepted
     * @return trimmed user string
     */
    public static String readString(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = SCANNER.nextLine();
            if (input != null) {
                input = input.trim();
            } else {
                input = "";
            }

            if (!input.isEmpty() || allowEmpty) {
                return input;
            }
            System.out.println("  [!] Input cannot be empty. Please try again.");
        }
    }

    /**
     * Safely reads an integer with validation.
     *
     * @param prompt message displayed to the user
     * @return valid integer
     */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = SCANNER.nextLine();
            if (line == null) {
                continue;
            }
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Please enter a valid integer.");
            }
        }
    }

    /**
     * Safely reads an integer within an inclusive range [min, max].
     *
     * @param prompt message displayed to the user
     * @param min minimum acceptable value
     * @param max maximum acceptable value
     * @return integer between min and max
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.printf("  [!] Please enter a value between %d and %d.%n", min, max);
        }
    }

    /**
     * Safely reads a double value with validation.
     *
     * @param prompt message displayed to the user
     * @return valid double
     */
    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = SCANNER.nextLine();
            if (line == null) {
                continue;
            }
            try {
                return Double.parseDouble(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid decimal number. Please try again.");
            }
        }
    }

    /**
     * Safely reads a double value greater than or equal to a minimum.
     *
     * @param prompt message displayed to the user
     * @param min minimum acceptable value
     * @return double greater than or equal to min
     */
    public static double readDouble(String prompt, double min) {
        while (true) {
            double value = readDouble(prompt);
            if (value >= min) {
                return value;
            }
            System.out.printf("  [!] Please enter a value greater than or equal to %.2f.%n", min);
        }
    }
}

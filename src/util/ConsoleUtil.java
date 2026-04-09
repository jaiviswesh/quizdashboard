package util;

import java.util.Scanner;

/**
 * Shared console I/O helpers.
 */
public class ConsoleUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    // ANSI colour codes (gracefully degraded if terminal doesn't support them)
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String CYAN    = "\u001B[36m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String BLUE    = "\u001B[34m";

    public static void printLine() {
        System.out.println(CYAN + "-".repeat(60) + RESET);
    }

    public static void printHeader(String title) {
        printLine();
        System.out.println(BOLD + CYAN + "  " + title + RESET);
        printLine();
    }

    public static void printSuccess(String msg) {
        System.out.println(GREEN + "+  " + msg + RESET);
    }

    public static void printError(String msg) {
        System.out.println(RED + "!  " + msg + RESET);
    }

    public static void printInfo(String msg) {
        System.out.println(YELLOW + "i  " + msg + RESET);
    }

    public static String prompt(String message) {
        System.out.print(BOLD + message + RESET + " ");
        return SCANNER.nextLine().trim();
    }

    public static int promptInt(String message, int min, int max) {
        while (true) {
            String input = prompt(message);
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
                printError("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                printError("Invalid input. Please enter a number.");
            }
        }
    }

    public static void pause() {
        prompt("\nPress ENTER to continue...");
    }

    public static void clearScreen() {
        // Works in most Unix terminals; safe no-op elsewhere
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printProgressBar(int current, int total) {
        int width = 30;
        int filled = (int) ((current / (double) total) * width);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++)
            bar.append(i < filled ? "=" : "-");
        bar.append("] ").append(current).append("/").append(total);
        System.out.println(CYAN + bar + RESET);
    }
}

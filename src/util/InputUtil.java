package util;

import java.util.Scanner;

/**
 * Helper methods for reading console input.
 *
 * <p>A single static {@link Scanner} backed by {@link System#in} is shared
 * across all calls to avoid accidentally closing standard input.</p>
 *
 * <p>All methods are static; the class cannot be instantiated.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 */
public class InputUtil {

    /** Shared scanner; never closed so that System.in stays open. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Prevents instantiation of this static utility class.
     */
    private InputUtil() {
    }

    /**
     * Displays a prompt and reads the next trimmed line from standard input.
     *
     * @param prompt text displayed before reading (e.g. {@code "Enter name: "})
     * @return trimmed user input; never {@code null}
     */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}

package ui;

import java.util.*;

/**
 * Tokenises and parses a Unix-like command line.
 *
 * <p>Tokens starting with {@code '-'} become options (next non-flag token
 * is the value, or {@code "true"} for boolean flags). All other tokens are
 * positional arguments.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 */
public class CommandParser {

    private final String commandLine;
    private String command;
    private List<String> args;
    private Map<String, String> options;

    /**
     * Constructs a parser and immediately tokenises the input.
     *
     * @param commandLine full command line string; trimmed before parsing
     */
    public CommandParser(String commandLine) {
        this.commandLine = commandLine.trim();
        this.args    = new ArrayList<>();
        this.options = new LinkedHashMap<>();
        parse();
    }

    /**
     * Splits the command line into a command, positional arguments, and
     * option/value pairs.
     * <p>
     * The parser is intentionally lightweight: quoted strings are not handled,
     * and whitespace is the only token separator. Options beginning with
     * {@code -} consume the following token as their value when that token is
     * not another option; otherwise the option is stored as a boolean flag with
     * value {@code "true"}.
     * </p>
     */
    private void parse() {
        String[] tokens = commandLine.split("\\s+");
        if (tokens.length == 0) { this.command = ""; return; }
        this.command = tokens[0];
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.startsWith("-")) {
                if (i + 1 < tokens.length && !tokens[i + 1].startsWith("-"))
                    options.put(token, tokens[++i]);
                else options.put(token, "true");
            } else args.add(token);
        }
    }

    /**
     * Returns the parsed command name.
     *
     * @return command name (first token), or empty string if input was blank
     */
    public String getCommand() { return command; }

    /**
     * Returns the parsed positional arguments.
     *
     * @return defensive copy of the positional argument list
     */
    public List<String> getArgs() { return new ArrayList<>(args); }

    /**
     * Checks whether an option flag was present.
     * @param optionName flag to query (e.g. {@code "-r"})
     * @return {@code true} if present
     */
    public boolean hasOption(String optionName) { return options.containsKey(optionName); }

    /**
     * Returns the value for an option flag.
     * @param optionName flag to query
     * @return value, or {@code null} if absent
     */
    public String getOptionValue(String optionName) { return options.get(optionName); }

    /**
     * Returns the value for an option flag, or a default when absent.
     * @param optionName   flag to query
     * @param defaultValue fallback value
     * @return value or default
     */
    public String getOptionValue(String optionName, String defaultValue) {
        return options.getOrDefault(optionName, defaultValue);
    }

    /**
     * Returns the number of positional arguments.
     *
     * @return number of positional arguments
     */
    public int getArgCount() { return args.size(); }

    /**
     * Returns the argument at the given index.
     * @param index zero-based index
     * @return argument value, or {@code null} if out of range
     */
    public String getArg(int index) { return index < args.size() ? args.get(index) : null; }

    /**
     * Returns the argument at the given index, or a default.
     * @param index        zero-based index
     * @param defaultValue fallback when out of range
     * @return argument or default
     */
    public String getArg(int index, String defaultValue) {
        return index < args.size() ? args.get(index) : defaultValue;
    }

    /**
     * Returns {@code true} if the argument count equals exactly {@code count}.
     * @param count required count
     * @return {@code true} on match
     */
    public boolean requiresArgs(int count) { return args.size() == count; }

    /**
     * Returns {@code true} if the argument count is at least {@code count}.
     * @param count minimum count
     * @return {@code true} if {@code >= count}
     */
    public boolean requiresMinArgs(int count) { return args.size() >= count; }

    /**
     * Returns a diagnostic representation of the parsed command line.
     *
     * @return a string containing the command, arguments, and options
     */
    @Override
    public String toString() {
        return "Command: " + command + ", Args: " + args + ", Options: " + options;
    }
}

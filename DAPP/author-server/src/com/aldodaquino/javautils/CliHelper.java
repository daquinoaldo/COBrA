package com.aldodaquino.javautils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Help parsing the args[]. You can add the option you want retrieve and this class will parse it automatically.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class CliHelper {

    private final ArrayList<CliOption> cliOptions = new ArrayList<>();
    private final ArrayList<CliFlag> cliFlags = new ArrayList<>();
    private int maxLongOptLength = 0;

    /**
     * Add available option for the CLI.
     * @param shortOpt like "-v".
     * @param longOpt like "--verbose".
     * @param hasValue true if the option must have a value (i.e. -o value),
     *                 false if is an option without value (i.e. --help).
     * @param description the description of the option to be shown in the help message.
     * @throws IllegalArgumentException if the shortOpt or the longOpt already exists in another option.
     */
    public void addOption(String shortOpt, String longOpt, boolean hasValue, String description) {
        if (!hasValue) addFlag(shortOpt, longOpt, description);
        // search for an already existent option
        for (CliOption cliOption : cliOptions) {
            if (cliOption.isEqual(shortOpt) || cliOption.isEqual(longOpt))
                throw new IllegalArgumentException("Option already exist.");
        }
        // add to the options
        cliOptions.add(new CliOption(shortOpt, longOpt, description));
    }

    // Internal auxiliary method
    private void addFlag(String shortOpt, String longOpt, String description) {
        // search for an already existent option
        for (CliFlag cliFlag : cliFlags) {
            if (cliFlag.isEqual(shortOpt) || cliFlag.isEqual(longOpt))
                throw new IllegalArgumentException("Option already exist.");
        }
        // add to the options
        cliFlags.add(new CliFlag(shortOpt, longOpt, description));
    }

    /**
     * Parse the String[] args of the main and saves the option value.
     * @param args the main's args.
     */
    public void parse(String[] args) {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            if (isNotAnOption(args[i])) System.err.println("Invalid option " + args[i] + ".");
            if (i + 1 < args.length && isNotAnOption(args[i + 1])) { // args[i+1] is the value of args[i]
                for (CliOption cliOption : cliOptions)
                    if (cliOption.parse(args[i], args[i+1])) break;
                i++;    // skip i + 1: is not an option
            } else { // args[i] has no value, so is a flag
                for (CliFlag cliFlag : cliFlags)
                    if (cliFlag.parse(args[i])) break;
            }
        }
    }

    /**
     * Return the list of all values of an option. For example -o value1 -o value2 will return [value1, value2].
     * @param opt the option in the short or the long format (will return the same list).
     * @return a String[] containing all the values, empty array if the option has no value or null if this option
     * doesn't exist.
     */
    public String[] getValues(String opt) {
        for (CliOption cliOption : cliOptions)
            if (cliOption.isEqual(opt)) return cliOption.getValues();
        return null;
    }

    /**
     * Return first value of an option. For example -o value1 -o value2 will return value1.
     * Use it for functions that are intended usable only once.
     * @param opt the option in the short or the long format (will return the same list).
     * @return the first value, empty string if the option has no value or null if this option doesn't exist.
     */
    public String getValue(String opt) {
        String[] values = getValues(opt);
        return values == null ? null : values.length == 0 ? "" : values[0];
    }

    /**
     * Return true if the program is launched with the specified option, false otherwise.
     * @param opt the option in the short or the long format (will return the same list).
     * @return true if the program is launched with the specified option, false otherwise.
     */
    public boolean isPresent(String opt) {
        for (CliFlag cliFlag : cliFlags)
            if (cliFlag.isEqual(opt))
                return cliFlag.isPresent();
        return false;
    }

    /**
     * Return a formatted help message showing the usage. The message has this format:
     * usage:
     * -o --longopt    an option description
     * -h --help       shows help
     * @return String of the message.
     */
    public String getHelpMessage() {
        // create a sorted collection with all the objects
        ArrayList<CliObject> cliObjects = new ArrayList<>();
        cliObjects.addAll(cliOptions);
        cliObjects.addAll(cliFlags);
        cliObjects.sort(Comparator.comparing(o -> o.shortOpt));

        // prepare the help string
        StringBuilder stringBuilder = new StringBuilder("Usage:\n");
        String initialString = "";
        for (CliObject cliObject : cliObjects) {
            stringBuilder.append(initialString).append(cliObject.shortOpt).append(" ").append(cliObject.longOpt);
            // append enough spaces to align the descriptions plus a tab (4 spaces) as separator
            for (int i = 0; i < maxLongOptLength - cliObject.longOpt.length() + 4; i++)
                stringBuilder.append(" ");
            stringBuilder.append(cliObject.description);
            initialString = "\n";   // from now append a line break before the new line
        }
        return stringBuilder.toString();
    }

    /**
     * Return a missing option message with this format:
     * Missing an option: -o -option    description of the option.
     * @param opt the short or long code of the option that you want (i.e. "o");
     * @return String of the message.
     */
    public String getMissingOptionMessage(String opt) {
        StringBuilder stringBuilder = new StringBuilder();
        Stream.concat(cliOptions.stream(),cliFlags.stream()).forEachOrdered(cliObject -> {
            if (cliObject.isEqual(opt))
                stringBuilder.append("Missing an option:\n").append(cliObject.shortOpt).append(" ")
                        .append(cliObject.longOpt).append("    ").append(cliObject.description);
        });
        return stringBuilder.toString();
    }

    // Internal auxiliary method
    private boolean isNotAnOption(String string) {
        return !string.contains("--") && !string.contains("-");
    }


    /* Auxiliary classes */

    private class CliObject {
        final String shortOpt;
        final String longOpt;
        String description;

        CliObject(String shortOpt, String longOpt, String description) {
            // add minuses if not present in the head of the string
            this.shortOpt = shortOpt.length() >= 1 && shortOpt.substring(0, 1).equals("-") ? shortOpt : "-" + shortOpt;
            this.longOpt = longOpt.length() >= 2 && longOpt.substring(0, 2).equals("--") ? longOpt : "--" + longOpt;

            if (shortOpt.length() > 2) throw new IllegalArgumentException("Short option must be a single letter.");
            if (longOpt.length() > maxLongOptLength) maxLongOptLength = longOpt.length();

            this.description = description;
        }

        boolean isEqual(String opt) {
            if (opt.length() >= 2 && opt.substring(0, 2).equals("--")) return longOpt.equals(opt);  // is a long option
            if (opt.length() >= 1 && opt.substring(0, 1).equals("-")) return shortOpt.equals(opt);  // is a short option
            return shortOpt.equals("-" + opt) || longOpt.equals("--" + opt);    // does not contains minuses
        }
    }

    private class CliOption extends CliObject {

        final ArrayList<String> values = new ArrayList<>();

        CliOption(String shortOpt, String longOpt, String description) {
            super(shortOpt, longOpt, description);
        }

        boolean parse(String opt, String value) {
            if (isEqual(opt)) {
                values.add(value);
                return true;
            }
            return false;
        }

        String[] getValues() {
            return values.toArray(new String[0]);
        }

    }

    private class CliFlag extends CliObject {

        boolean found;

        CliFlag(String shortOpt, String longOpt, String description) {
            super(shortOpt, longOpt, description);
        }

        boolean parse(String opt) {
            if (isEqual(opt)) {
                found = true;
                return true;
            }
            return false;
        }

        boolean isPresent() {
            return found;
        }

    }

}

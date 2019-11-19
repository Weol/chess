package net.rahka.paramaters;

/**
 * This class defines a parameter with a name, short-hand name, and description. It is used
 * by {@link ParameterInterpreter} and {@link ParameterInterpretation} to handle the parameters.
 *
 * @param <T> the type that the flag should parse to
 */
public class Flag<T> {

    private String name;
    private String flag;
    private String description;
    private boolean required;

    /**
     * Constructs a new flag with the given properties
     *
     * @param name the name of the flag
     * @param flag the short-hand name
     * @param description the description
     * @param required whether or not this flag is required.
     */
    public Flag(String name, String flag, String description, boolean required) {
        this.name = name;
        this.flag = flag;
        this.description = description;
        this.required = required;
    }

    /**
     * Construct a non-required flag with the given properties
     *
     * @param name the name of the flag
     * @param flag the short-hand name
     * @param description the description
     */
    public Flag(String name, String flag, String description) {
        this(name, flag, description, false);
    }

    /**
     * Used by {@link ParameterInterpreter} to check if the flag requires on argument
     *
     * @return wther it requires on argument
     */
    protected boolean expectsArgument() {
        return false;
    };

    /**
     * Parses this flags argument
     *
     * @param arg argument to parse
     *
     * @return the parsed value
     */
    protected T parseArgument(String arg) {
        return null;
    }

    /**
     * Returns whether or not this flag is required (non-optional).
     *
     * @return required
     */
    public boolean isRequired() {
        return required;
    }

    public String getName() {
        return name;
    }

    public String getFlag() {
        return flag;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.name;
    }

}

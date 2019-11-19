package net.rahka.paramaters;

import java.util.function.Function;

/**
 * This class is a Flag that requires an argument and parses this argument.
 */
public class FunctionFlag extends Flag<Object> {

    Function<String, Object> function;

    /**
     * Constructs a new FunctionFlag with the given properties and a parse function.
     *
     * @param name the name of the flag
     * @param flag the short-hand name of the flag
     * @param description the description of the flag
     * @param function the parsing function of the flag
     * @param required whether or not this flag is required
     */
    public FunctionFlag(String name, String flag, String description, Function<String, Object> function, boolean required) {
        super(name, flag, description, required);
        this.function = function;
    }

    /**
     * Constructs a new FunctionFlag with the given properties and a parse function.
     *
     * @param name the name of the flag
     * @param flag the short-hand name of the flag
     * @param description the description of the flag
     * @param function the parsing function of the flag
     */
    public FunctionFlag(String name, String flag, String description, Function<String, Object> function) {
        this(name, flag, description, function, false);
    }

    @Override
    protected boolean expectsArgument() {
        return true;
    }

    @Override
    protected Object parseArgument(String arg) {
        return function.apply(arg);
    }

}

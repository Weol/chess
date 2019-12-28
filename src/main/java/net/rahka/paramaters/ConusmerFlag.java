package net.rahka.paramaters;

import java.util.function.Consumer;

/**
 * A flag that accepts a string argument and calls a {@link Consumer<String>} with the argument. It will not produce
 * any value that can be retrieved by {@link ParameterInterpretation#get}.
 */
public class ConusmerFlag extends Flag<String> {

    Consumer<String> consumer;

    /**
     * Constructs a new ConsumerFlag with the given properties and a consumer that will be
     * run if the flag is present.
     *
     * @param name the name of the flag
     * @param flag the short-hand name of the flag
     * @param description the description of the flag
     * @param consumer the consumer to be run if flag is present
     * @param required whether or not it is required
     */
    public ConusmerFlag(String name, String flag, String description, Consumer<String> consumer, boolean required) {
        super(name, flag, description, required);
        this.consumer = consumer;
    }

    /**
     * Constructs a new ConsumerFlag with the given properties and a consumer that will be
     * run if the flag is present.
     *
     * @param name the name of the flag
     * @param flag the short-hand name of the flag
     * @param description the description of the flag
     * @param consumer the consumer to be run if flag is present
     */
    public ConusmerFlag(String name, String flag, String description, Consumer<String> consumer) {
        this(name, flag, description, consumer, false);
    }

    @Override
    protected boolean expectsArgument() {
        return true;
    }

    @Override
    protected String parseArgument(String arg) {
        consumer.accept(arg);
        return null;
    }

}

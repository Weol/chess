package net.rahka.paramaters;

/**
 * This class is Flag that runs a runnable if the flag is present
 */
public class RunnableFlag extends Flag<String> {

    Runnable runnable;

    /**
     * Constructs a new RunnableFlag with the given properties and a runnable that is run if the
     * flag is present.
     *
     * @param name the name of the flag
     * @param flag the short-hand name of the flag
     * @param description the description of the flag
     * @param runnable the runnable to be run
     * @param required whether or not it is required
     */
    public RunnableFlag(String name, String flag, String description, Runnable runnable, boolean required) {
        super(name, flag, description, required);
        this.runnable = runnable;
    }

    /**
     * Constructs a new RunnableFlag with the given properties and a runnable that is run if the
     * flag is present.
     *
     * @param name the name of the flag
     * @param flag the short-hand name of the flag
     * @param description the description of the flag
     * @param runnable the runnable to be run
     */
    public RunnableFlag(String name, String flag, String description, Runnable runnable) {
        this(name, flag, description, runnable, false);
    }

    @Override
    protected boolean expectsArgument() {
        return false;
    }

    @Override
    protected String parseArgument(String arg) {
        runnable.run();
        return null;
    }

}

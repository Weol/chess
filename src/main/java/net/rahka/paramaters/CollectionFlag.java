package net.rahka.paramaters;

import java.util.Collection;

/**
 * This class is a Flag that only accepts arugments that are contained in a list.
 */
public class CollectionFlag extends Flag<String> {

    private Collection<String> collection;

    /**
     * Constructs a new ListFlag with the given properties and a list of accepted arguments
     *
     * @param name the name of flag
     * @param flag the short-hand name of the flag
     * @param description the description of the flag
     * @param collection the list of accepted arguments
     * @param required whether or not the flag is required
     */
    public CollectionFlag(String name, String flag, String description, Collection<String> collection, boolean required) {
        super(name, flag, description, required);
        this.collection = collection;
    }

    /**
     * Constructs a new ListFlag with the given properties and a list of accepted arguments
     *
     * @param name the name of flag
     * @param flag the short-hand name of the flag
     * @param description the description of the flag
     * @param collection the list of accepted arguments
     */
    public CollectionFlag(String name, String flag, String description, Collection<String> collection) {
        this(name, flag, description, collection, false);
    }

    @Override
    public boolean expectsArgument() {
        return true;
    }

    @Override
    protected String parseArgument(String arg) {
        if (!collection.contains(arg)) {
            String string = "Invalid option for [" + getName() + "]\n";
            string += "\tValid options:";
            for (String s : collection) {
                string += s + "\n\t\t";
            }
            throw new ParamaterException(string);
        }
        return arg;
    }

}
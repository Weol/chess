package net.rahka.paramaters;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class is on interpretation of a set of argument provided to the {@link ParameterInterpreter} class.
 * It contains the interpreted parameters and provides methods to retrieve and check specific parameters.
 */

public class ParameterInterpretation {

	protected HashMap<String, Object> flags;
	protected LinkedList<Flag> required;

	protected ParameterInterpretation() {
		required = new LinkedList<>();
		flags = new HashMap<>();
	}

    /**
     * Returns the parsed value of a parameter if the parameter is present.
     * Throws NullPointerException if a parameter is not set. Use {@link #has(String)}
     * before calling this or use the {@link #get(String, Object)}.
     *
     * @param name the name of the parameter
     *
     * @return the parsed parameter object
     */
	public <T> T get(String name) {
		return (T) flags.get(name);
	}

    /**
     * Returns the parsed value of a parameter, if the parameter is not present then
     * it will return the alt object. Thhis method exists so we can cut out bloating
     * if statements to check if on optional parameter is set.
     *
     * @param name the name if the parameter
     * @param alt the object to be returned if the parameter is not present
     *
     * @return the parameter or alt
     */
	public <T> T get(String name, T alt) {
		if (has(name)) {
			return (T) flags.get(name);
		} else {
			return alt;
		}
	}

    /**
     * Checks whether or not a parameter is present.
     *
     * @param string the name of the parameter
     *
     * @return if it is present
     */
	public boolean has(String string) {
		return flags.containsKey(string);
	}

    /**
     * Used by {@link ParameterInterpreter} to add parsed flags to the intepretation
     *
     * @param flag the flag to add
     * @param object the parsed value
     */
	protected void addFlag(Flag flag, Object object) {
		flags.put(flag.getName(), object);
		if (required.contains(flag)) {
		    required.remove(flag);
        }
	}

    /**
     * Used by {@link ParameterInterpreter} to add parsed flags to the intepretation
     *
     * @param flag the flag to add
     */
	protected void addFlag(Flag flag) {
		flags.put(flag.getName(), null);
	}

    /**
     * Used by {@link ParameterInterpreter} to add the interpreters required flags
     *
     * @param flags the required flags
     */
	protected void prepareRequired(Flag[] flags) {
		for (Flag flag : flags) {
            required.addLast(flag);
		}
	}

    /**
     * Used by {@link ParameterInterpreter} to pop a required flag from the interpretation
     *
     * @return
     */
	protected Flag popRequired() {
		if (required.isEmpty()) {
			throw new ParamaterException("Too many arguments!");
		}
	
		return required.removeFirst();
	}
	
}

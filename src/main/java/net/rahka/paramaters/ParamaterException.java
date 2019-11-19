package net.rahka.paramaters;

/**
 * An exception used to indicate that there was on invalid argument supplied to a {@link ParameterInterpreter}
 */
public class ParamaterException extends RuntimeException {

	public ParamaterException(String string) {
		super(string);
	}

	public ParamaterException(String string, Object ... args) {
		super(String.format(string,args));
	}

}

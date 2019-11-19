package net.rahka.paramaters;

import java.util.*;

/**
 * This class provides argument handling in the form of strings. When constructed it can be given
 * a set of {@link Flag} which it will check for and parse.
 */
public class ParameterInterpreter {
	
	private HashMap<String, Flag> flags;
	private LinkedList<Flag> required;

	public String prefix = "-";

    /**
     * Constructs a new ParameterInterpreter that will check and parse the supplied flags.
     *
     * @param flags the flags
     */
	public ParameterInterpreter(Flag ...flags) {
		this.flags = new HashMap<>();
		this.required = new LinkedList<>();

		//Add help flag before any other flag so that it can be overridden
        Flag helpFlag = new RunnableFlag("help", "?", "Displays a list of commands", this::printHelp);
		this.flags.put("help", helpFlag);
        this.flags.put("?", helpFlag);

		for (Flag flag : flags) {
			this.flags.put(flag.getName(), flag);
			this.flags.put(flag.getFlag(), flag);
			if (flag.isRequired()) {
				required.addLast(flag);
			}
		}
	}

    /**
     * Interprets on array of parameters.
     *
     * @param args the parameters to intepret
     *
     * @return the interpreted parameters
     */
	public ParameterInterpretation intepret(String[] args) {
		ParameterInterpretation intepretation = new ParameterInterpretation();
		intepretation.prepareRequired(required.toArray(new Flag[required.size()]));
		
		ListIterator<String> iterator = Arrays.asList(args).listIterator();
		
		try {
			while(iterator.hasNext()) {
				String arg = iterator.next();
				if (arg.startsWith(prefix + prefix)) {
					arg = arg.substring(prefix.length()*2);
					intepretFlag(intepretation, iterator, arg);
				} else if (arg.startsWith(prefix)) {
					arg = arg.substring(prefix.length());
                    intepretFlag(intepretation, iterator, arg);
				} else {
                    arg = intepretation.popRequired().getName();
                    iterator.previous();
					intepretFlag(intepretation, iterator, arg);
				}
			}
			
			if (!intepretation.required.isEmpty()) {
				String[] missing = new String[intepretation.required.size()];

				Iterator<Flag> requiredIterator = intepretation.required.iterator();
				int i = 0;
				while (requiredIterator.hasNext()) {
				    missing[i++] = "[" + requiredIterator.next().getName() + "]";
                }
				printHelp();
				throw new ParamaterException("Missing arguments: " + String.join(", ", missing));
			}
		} catch (ParamaterException e) {
            System.out.println(e.getMessage());
			System.exit(1);
		}
		
		return intepretation;
	}

	private void intepretFlag(ParameterInterpretation intepretation, Iterator<String> iterator, String arg) {
		if (this.flags.containsKey(arg)) {
			Flag flag = this.flags.get(arg);
			if (flag.expectsArgument()) {
				if (!iterator.hasNext()) {
					throw new ParamaterException("Missing argument for: " + prefix + arg);
				}

				intepretation.addFlag(flag, flag.parseArgument(iterator.next()));
			} else {
				flag.parseArgument(null);
				intepretation.addFlag(flag);
			}
		} else {
			throw new ParamaterException("Unknown paramater: " + arg);
		}
	}

	private void printHelp() {
		System.out.print("USAGE: ");
		required.forEach((Flag flag) -> System.out.print("[" + flag.getName().toUpperCase() + "] "));
		flags.values().stream().distinct().forEach((Flag flag) -> System.out.printf("\t%s%s, %s%s\n\t\t%s\n", prefix, flag.getFlag(), prefix+prefix, flag.getName(), flag.getDescription()));
		throw new ParamaterException("");
	}

}

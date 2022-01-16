package net.rahka.chess.configuration;

import lombok.extern.java.Log;
import org.yaml.snakeyaml.Yaml;

@Log
public class YamlConfiguration extends Configuration {

	public YamlConfiguration(String basePackage) {
		super(basePackage);
	}

	public YamlConfiguration(String basePackage, Class<?>... configurationClasses) {
		super(basePackage, configurationClasses);
	}

	/**
	 * Constructs an instnace of T with a specific name, and configures its configurable members using values
	 * found in a Yaml file.
	 */
	public <T> T create(Class<T> cls, Yaml yaml, String name) {
		var configurableClasses = find(cls);

		var matchingClass = configurableClasses.stream()
				.filter(x -> x.getName().equals(name))
				.findFirst();

		if (matchingClass.isEmpty()) {
			throw new NonConfigurableClassException(String.format("Could not find class of type %s and name \"%s\"", cls.getName(), name));
		}

		var configurableClass = matchingClass.get();
		for (ConfigurableItem dependency : configurableClass.getDependencies()) {
			if (dependency.isConfigurableInteger()) {
				configureInteger(dependency.asConfigurableInteger(), yaml);
			} else if (dependency.isConfigurableFloatingPoint()) {
				configureFloatingPoint(dependency.asConfigurableFloatingPoint(), yaml);
			} else if (dependency.isConfigurableBoolean()) {
				configureBoolean(dependency.asConfigurableBoolean(), yaml);
			} else if (dependency.isConfigurableString()) {
				configureString(dependency.asConfigurableString(), yaml);
			}
		}
		return null;
	}

	private void configureFloatingPoint(ConfigurableFloatingPointItem configurableFloatingPoint, Yaml yaml) {
		//configurableFloatingPoint.setSupplier();
	}

	private void configureBoolean(ConfigurableBooleanItem configurableBoolean, Yaml yaml) {
	}

	private void configureString(ConfigurableStringItem configurableString, Yaml yaml) {
	}

	private void configureInteger(ConfigurableIntegerItem configurableInteger, Yaml yaml) {
	}

}
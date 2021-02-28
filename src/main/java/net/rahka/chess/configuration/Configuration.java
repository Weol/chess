package net.rahka.chess.configuration;

import lombok.Getter;
import lombok.extern.java.Log;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Log
public class Configuration {

	@Getter
	private final String basePackage;

	@Getter
	private final Class<?> configurationClass;

	private final Map<Class<?>, Set<Invokable>> constructable = new HashMap<>();

	public Configuration(String basePackage) {
		this(basePackage, null);
	}

	public Configuration(String basePackage, Class<?> configurationClass) {
		this.basePackage = basePackage;
		this.configurationClass = configurationClass;

		var reflections = new Reflections(basePackage);

		//Classes that depend on the key
		final Map<Class<?>, List<Invokable>> classesDependsOn = new HashMap<>();

		//List of classes that are dependencies of key
		final Map<Invokable, List<? extends Class<?>>> classDependencies = new HashMap<>();

		var frontier = new LinkedList<Invokable>();

		var configurables = new HashSet<Invokable>();

		if (configurationClass != null) {
			for (Method method : configurationClass.getDeclaredMethods()) {
				if (method.isAnnotationPresent(Configurable.class) && Modifier.isStatic(method.getModifiers())) {
					method.setAccessible(true);

					var params = new ArrayList<Invokable.InvokableParameter>(method.getParameterCount());
					for (Parameter parameter : method.getParameters()) {
						params.add(new Invokable.InvokableParameter(parameter.getType(), findParameterName(parameter), findConfigurableAnnotation(parameter)));
					}

					var annotation = method.getAnnotation(Configurable.class);
					String name = (annotation.name().length() > 0) ? annotation.name() : null;

					var invokable = new Invokable(name, method.getReturnType(), params) {
						@Override
						Object invoke(Object... params) throws InvocationTargetException, IllegalAccessException {
							return method.invoke(null, params);
						}
					};

					configurables.add(invokable);
				}
			}
		}

		for (Class<?> cls : reflections.getTypesAnnotatedWith(Configurable.class)) {
			if (cls.getConstructors().length != 1) {
				log.warning(String.format("Ignoring configurable class %s (Class must have exactly 1 constructor)", cls.getName()));
				continue;
			}

			var annotation = cls.getAnnotation(Configurable.class);
			String name = (annotation.name().length() > 0) ? annotation.name() : null;

			var params = new ArrayList<Invokable.InvokableParameter>(cls.getConstructors()[0].getParameterCount());
			for (Parameter parameter : cls.getConstructors()[0].getParameters()) {
				params.add(new Invokable.InvokableParameter(parameter.getType(), findParameterName(parameter), findConfigurableAnnotation(parameter)));
			}

			var invokable = new Invokable(name, cls, params) {

				@Override
				Object invoke(Object... params) throws InvocationTargetException, IllegalAccessException, InstantiationException {
					return cls.getConstructors()[0].newInstance(params);
				}

			};

			configurables.add(invokable);
		}

		for (Invokable invokable : configurables) {
			var dependencies = invokable.getParameters().stream()
					.map(Invokable.InvokableParameter::getCls)
					.filter(cls -> !cls.isPrimitive())
					.filter(cls -> !cls.equals(String.class))
					.collect(Collectors.toCollection(ArrayList::new));

			if (dependencies.size() == 0) {
				frontier.add(invokable);
			} else {
				classDependencies.put(invokable, dependencies);
			}

			for (Class<?> dependency : dependencies) {
				classesDependsOn.compute(dependency, (k, v) -> {
					v = (v == null) ? new ArrayList<>() : v;
					v.add(invokable);
					return v;
				});
			}
		}

		var topologicallySorted = new LinkedList<Invokable>();
		while (!frontier.isEmpty()) {
			var baseInvokable = frontier.removeFirst();

			topologicallySorted.add(baseInvokable);

			var extended = getSuperClassesAndInterfaces(baseInvokable.getType());

			for (Class<?> current : extended) {
				var dependent = classesDependsOn.get(current);
				if (dependent != null) {
					for (Invokable invokable : dependent) {
						var dependencies = classDependencies.get(invokable);
						dependencies.remove(current);

						if (dependencies.isEmpty()) {
							classDependencies.remove(invokable);
							frontier.add(invokable);
						}
					}

					classesDependsOn.remove(current);
				}
			}
		}

		for (var entry : classDependencies.entrySet()) {
			var unresolvedNames = entry.getValue().stream()
					.map(Class::getName)
					.toArray(String[]::new);

			log.severe(String.format(
					"Cannot construct configurable class %s because of unresolved dependencies (%s)",
					entry.getKey().getType().getName(),
					String.join(", ", unresolvedNames)));
		}

		for (Invokable invokable : topologicallySorted) {
			var instances = getSuperClassesAndInterfaces(invokable.getType());

			for (Class<?> instance : instances) {
				constructable.compute(instance, (k, v) -> {
					v = (v == null) ? new HashSet<>() : v;
					v.add(invokable);
					return v;
				});
			}
		}
	}

	private String findParameterName(Parameter parameter) {
		String name = "";
		if (parameter.isAnnotationPresent(Configurable.class)) {
			name = parameter.getAnnotation(Configurable.class).name();
		} else if (parameter.isAnnotationPresent(ConfigurableInt.class)) {
			name = parameter.getAnnotation(ConfigurableInt.class).name();
		} else if (parameter.isAnnotationPresent(ConfigurableFloatingPoint.class)) {
			name = parameter.getAnnotation(ConfigurableFloatingPoint.class).name();
		} else if (parameter.isAnnotationPresent(ConfigurableString.class)) {
			name = parameter.getAnnotation(ConfigurableString.class).name();
		} else if (parameter.isAnnotationPresent(ConfigurableBoolean.class)) {
			name = parameter.getAnnotation(ConfigurableBoolean.class).name();
		}

		if (name.length() == 0) return parameter.getName();
		return name;
	}

	private Annotation findConfigurableAnnotation(Parameter parameter) {
		if (parameter.isAnnotationPresent(Configurable.class)) {
			return parameter.getAnnotation(Configurable.class);
		} else if (parameter.isAnnotationPresent(ConfigurableInt.class)) {
			return parameter.getAnnotation(ConfigurableInt.class);
		} else if (parameter.isAnnotationPresent(ConfigurableFloatingPoint.class)) {
			return parameter.getAnnotation(ConfigurableFloatingPoint.class);
		} else if (parameter.isAnnotationPresent(ConfigurableString.class)) {
			return parameter.getAnnotation(ConfigurableString.class);
		} else if (parameter.isAnnotationPresent(ConfigurableBoolean.class)) {
			return parameter.getAnnotation(ConfigurableBoolean.class);
		}
		return null;
	}

	private Set<Class<?>> getSuperClassesAndInterfaces(Class<?> base) {
		return getSuperClassesAndInterfaces(base, new HashSet<>());
	}

	private Set<Class<?>> getSuperClassesAndInterfaces(Class<?> cls, Set<Class<?>> classes) {
		do {
			classes.add(cls);

			Class<?>[] interfaces = cls.getInterfaces();
			for (Class<?> i : interfaces) {
				getSuperClassesAndInterfaces(i, classes);
			}

			if (cls.getSuperclass() == null) {
				break;
			}

			cls = cls.getSuperclass();
		} while (!"java.lang.Object".equals(cls.getCanonicalName()));

		return classes;
	}

	public <T> Set<ConfigurableClass<T>> find(Class<T> cls) {
		return find(cls, null);
	}

	<T> Set<ConfigurableClass<T>> find(Class<T> cls, Set<String> except) {
		var invokables = constructable.get(cls);
		if (invokables == null || invokables.isEmpty()) {
			throw new NonConfigurableClassException("Cannot find configurable class " + cls.getName());
		} else {
			var set = new HashSet<ConfigurableClass<T>>();
			for (var invokable : invokables) {
				if (except == null || !except.contains(invokable.getType().getName())) {
					set.add(new ConfigurableClass<>(invokable, this, except));
				}
			}
			return set;
		}
	}

}

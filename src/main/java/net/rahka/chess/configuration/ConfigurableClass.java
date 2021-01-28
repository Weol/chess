package net.rahka.chess.configuration;

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

public class ConfigurableClass<T> {

    private static boolean isInteger(Class<?> cls) {
        return (cls.equals(int.class)
                || cls.equals(long.class)
                || cls.equals(short.class)
                || cls.equals(byte.class)
                || cls.equals(char.class));
    }

    private static boolean isFloatingPoint(Class<?> cls) {
        return (cls.equals(double.class) || cls.equals(float.class));
    }

    private static boolean isString(Class<?> cls) {
        return (cls.equals(String.class));
    }

    private static boolean isBoolean(Class<?> cls) {
        return (cls.equals(boolean.class));
    }

    private final Configuration configuration;

    private final Invokable invokable;

    @Getter
    private final Class<T> cls;

    @Getter
    private final String name;

    @Getter
    private final ConfigurableItem[] dependencies;

    public ConfigurableClass(Invokable invokable, Configuration configuration) {
        this.invokable = invokable;
        this.configuration = configuration;
        this.cls = (Class<T>) invokable.getType();
        this.name = invokable.name;

        dependencies = buildDependencies();
    }

    private ConfigurableItem[] buildDependencies() {
        var list = new ConfigurableItem[invokable.getParameters().size()];
        buildDependencies(list);
        return list;
    }

    private void buildDependencies(ConfigurableItem[] list) {
        if (!invokable.getParameters().isEmpty()) {
            int i = 0;
            for (var parameter : invokable.getParameters()) {
                if (parameter.getCls().isPrimitive() || parameter.getCls().equals(String.class)) {
                    if (isInteger(parameter.getCls())) {
                        ConfigurableInt configurable = null;
                        if (parameter.getAnnotation() instanceof ConfigurableInt) {
                            configurable = (ConfigurableInt) parameter.getAnnotation();
                        }

                        list[i++] = new ConfigurableIntegerItem(parameter.getCls(), parameter.getName(), configurable);
                    } else if (isFloatingPoint(parameter.getCls())) {
                        ConfigurableFloatingPoint configurable = null;
                        if (parameter.getAnnotation() instanceof ConfigurableFloatingPoint) {
                            configurable = (ConfigurableFloatingPoint) parameter.getAnnotation();
                        }

                        list[i++] = new ConfigurableFloatingPointItem(parameter.getCls(), parameter.getName(), configurable);
                    } else if (isBoolean(parameter.getCls())) {
                        ConfigurableBoolean configurable = null;
                        if (parameter.getAnnotation() instanceof ConfigurableBoolean) {
                            configurable = (ConfigurableBoolean) parameter.getAnnotation();
                        }

                        list[i++] = new ConfigurableBooleanItem(parameter.getName(), configurable);
                    } else if (isString(parameter.getCls())) {
                        ConfigurableString configurable = null;
                        if (parameter.getAnnotation() instanceof ConfigurableString) {
                            configurable = (ConfigurableString) parameter.getAnnotation();
                        }

                        list[i++] = new ConfigurableStringItem(parameter.getName(), configurable);
                    }
                } else {
                    var implementations = new HashSet<ConfigurableClass<?>>(configuration.find(parameter.getCls()));

                    Configurable configurable = null;
                    if (parameter.getAnnotation() instanceof Configurable) {
                        configurable = (Configurable) parameter.getAnnotation();
                    }

                    list[i++] = new ConfigurableClassItem(parameter.getCls(), implementations, parameter.getName(), configurable);
                }
            }
        }
    }

    public T build() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        var args = new Object[dependencies.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = dependencies[i].getSupplier().get();
        }
        return (T) invokable.invoke(args);
    }

    @Override
    public String toString() {
        return getName();
    }

}

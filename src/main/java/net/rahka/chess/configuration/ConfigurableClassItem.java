package net.rahka.chess.configuration;

import lombok.Getter;

import java.util.Set;

public class ConfigurableClassItem extends ConfigurableItem {

    @Getter
    private final Class<?> def;

    @Getter
    private Set<ConfigurableClass<?>> classes;

    public ConfigurableClassItem(Class<?> cls, Set<ConfigurableClass<?>> classes, String name, Configurable configurable) {
        super(cls, name);

        this.classes = classes;
        this.def = (configurable != null) ? configurable.def() : null;
    }
}

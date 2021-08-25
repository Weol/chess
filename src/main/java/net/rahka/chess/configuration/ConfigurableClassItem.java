package net.rahka.chess.configuration;

import lombok.Getter;

import java.util.Set;

public class ConfigurableClassItem extends ConfigurableItem {

    @Getter
    private ConfigurableClass<?> def;

    @Getter
    private Set<ConfigurableClass<?>> classes;

    public ConfigurableClassItem(Class<?> cls, Set<ConfigurableClass<?>> classes, String name, Configurable configurable) {
        super(cls, name);

        this.classes = classes;
        if (configurable != null) {
            for (var clazz : classes) {
                if (clazz.getCls().equals(configurable.def())) {
                    def = clazz;
                }
            }
        }

        if (def == null && classes.size() > 0) {
            for (var clazz : classes) {
                def = clazz;
                break;
            }
        }
    }

    public Class<?> getCls() {
        return cls;
    }

}

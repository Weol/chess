package net.rahka.chess.configuration;

import lombok.Getter;

public class ConfigurableFloatingPointItem extends ConfigurableItem {

    @Getter
    private final double max, min, def;

    public ConfigurableFloatingPointItem(Class<?> cls, String name, ConfigurableFloatingPoint configurable) {
        super(cls, name);

        if (configurable != null) {
            max = configurable.max();
            min = configurable.min();
            def = configurable.def();
        } else {
            max = Double.MAX_VALUE;
            min = -Double.MAX_VALUE;
            def = 0;
        }
    }

}

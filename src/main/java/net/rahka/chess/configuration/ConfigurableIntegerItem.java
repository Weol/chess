package net.rahka.chess.configuration;

import lombok.Getter;

public class ConfigurableIntegerItem extends ConfigurableItem {

    @Getter
    private final long max, min, def;

    public ConfigurableIntegerItem(Class<?> cls, String name, ConfigurableInt configurable) {
        super(cls, name);

        if (configurable != null) {
            max = configurable.max();
            min = configurable.min();
            def = configurable.def();
        } else {
            max = Long.MAX_VALUE;
            min = Long.MIN_VALUE;
            def = 0;
        }
    }

}

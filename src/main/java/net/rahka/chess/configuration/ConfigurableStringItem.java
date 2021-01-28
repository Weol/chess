package net.rahka.chess.configuration;

import lombok.Getter;

public class ConfigurableStringItem extends ConfigurableItem {

    @Getter
    private final String def;

    public ConfigurableStringItem(String name, ConfigurableString configurable) {
        super(String.class, name);

        def = (configurable != null) ? configurable.def() : "";
    }

}

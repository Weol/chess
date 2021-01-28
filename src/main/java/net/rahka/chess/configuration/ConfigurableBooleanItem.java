package net.rahka.chess.configuration;

public class ConfigurableBooleanItem extends ConfigurableItem{

    private final boolean def;

    public ConfigurableBooleanItem(String name, ConfigurableBoolean configurable) {
        super(Boolean.class, name);

        def = configurable != null && configurable.def();
    }

    public boolean getDef() {
        return def;
    }

}

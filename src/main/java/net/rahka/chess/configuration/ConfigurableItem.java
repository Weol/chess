package net.rahka.chess.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ConfigurableItem {

    final Class<?> cls;

    @Getter
    private final String name;

    @Getter(AccessLevel.PACKAGE) @Setter
    private ParameterSupplier<?> supplier;

    public boolean isConfigurableInteger() {
        return (this instanceof ConfigurableIntegerItem);
    }

    public boolean isConfigurableFloatingPoint() {
        return (this instanceof ConfigurableFloatingPointItem);
    }

    public boolean isConfigurableBoolean() {
        return (this instanceof ConfigurableBooleanItem);
    }

    public boolean isConfigurableString() {
        return (this instanceof ConfigurableStringItem);
    }

    public boolean isConfigurableClass() {
        return (this instanceof ConfigurableClassItem);
    }

    public ConfigurableIntegerItem asConfigurableInteger() {
        if (!isConfigurableInteger()) throw new ClassCastException("Class cannot be represented as a integer");

        return (ConfigurableIntegerItem) this;
    }

    public ConfigurableFloatingPointItem asConfigurableFloatingPoint() {
        if (!isConfigurableFloatingPoint()) throw new ClassCastException("Class cannot be represented as a floating point");

        return (ConfigurableFloatingPointItem) this;
    }

    public ConfigurableBooleanItem asConfigurableBoolean() {
        if (!isConfigurableBoolean()) throw new ClassCastException("Class cannot be represented as a boolean");

        return (ConfigurableBooleanItem) this;
    }

    public ConfigurableStringItem asConfigurableString() {
        if (!isConfigurableString()) throw new ClassCastException("Class cannot be represented as a string");

        return (ConfigurableStringItem) this;
    }

    public ConfigurableClassItem asConfigurableClass() {
        if (!isConfigurableClass()) throw new ClassCastException("Class cannot be represented as a class");

        return (ConfigurableClassItem) this;
    }

}

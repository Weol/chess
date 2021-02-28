package net.rahka.chess.configuration;

import lombok.Getter;

public class ConfigurableIntegerItem extends ConfigurableItem {

    public static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }

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

    @Override
    ParameterSupplier<?> getSupplier() {
        var actual = super.getSupplier();

        return () -> {
            Number value = (Number) actual.get();
            if (cls.equals(int.class)) {
                return value.intValue();
            } else if (cls.equals(long.class)) {
                return value.longValue();
            } else if (cls.equals(byte.class)) {
                return value.byteValue();
            } else if (cls.equals(short.class)) {
                return value.shortValue();
            } else if (cls.equals(char.class)) {
                return (char) value.byteValue();
            }
            throw new IllegalArgumentException("wtf");
        };
    }
}

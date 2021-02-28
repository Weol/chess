package net.rahka.chess.configuration;

import java.lang.reflect.InvocationTargetException;

public interface ParameterSupplier<T> {

    T get() throws IllegalAccessException, InstantiationException, InvocationTargetException;

}

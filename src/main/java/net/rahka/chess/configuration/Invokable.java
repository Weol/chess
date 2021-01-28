package net.rahka.chess.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

abstract class Invokable {

    @Getter
    final Class<?> type;

    @Getter
    final List<InvokableParameter> parameters;

    @Getter
    final String name;

    public Invokable(String name, Class<?> type, List<InvokableParameter> parameters) {
        this.type = type;
        this.parameters = parameters;
        this.name = (name != null) ? name : type.getSimpleName();
    }

    abstract Object invoke(Object... params) throws InvocationTargetException, IllegalAccessException, InstantiationException;

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @RequiredArgsConstructor
    public static class InvokableParameter {

        @Getter
        private final Class<?> cls;

        @Getter
        private final String name;

        @Getter
        private final Annotation annotation;

    }

}

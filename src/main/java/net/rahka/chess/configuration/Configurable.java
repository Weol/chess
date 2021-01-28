package net.rahka.chess.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {

    String name() default "";

    Class<?> def() default Configurable.class;

}

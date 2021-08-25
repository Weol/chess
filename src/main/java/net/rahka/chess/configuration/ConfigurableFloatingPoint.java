package net.rahka.chess.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurableFloatingPoint {

    String name();

    double min() default -Double.MAX_VALUE;

    double max() default Double.MAX_VALUE;

    double def() default 0;

}

package net.rahka.chess.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurableInt {

    String name() default "";

    long min() default Integer.MIN_VALUE;

    long max() default Integer.MAX_VALUE;

    long def() default 0;

}

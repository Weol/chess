package net.rahka.chess.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurableBoolean {

    String name() default "";

    boolean def() default true;

}

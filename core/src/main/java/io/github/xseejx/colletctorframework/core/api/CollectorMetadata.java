package io.github.xseejx.colletctorframework.core.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CollectorMetadata {
    String name();
    String description() default "";
    String[] tags() default {};
    boolean threadSafe() default true;
    CollectorParameter[] parameters() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    public @interface CollectorParameter {
        String key();
        ParameterType type() default ParameterType.STRING;
        String defaultValue() default "";
        boolean required() default false;
    }

    public enum ParameterType {
        STRING,
        BOOLEAN,
        INTEGER,
        PATH
    }
}
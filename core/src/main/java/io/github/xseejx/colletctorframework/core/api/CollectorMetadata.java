package io.github.xseejx.colletctorframework.core.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CollectorMetadata {
    String name();
    String description()    default "";
    String[] tags()         default {};
    boolean threadSafe()    default true;
}
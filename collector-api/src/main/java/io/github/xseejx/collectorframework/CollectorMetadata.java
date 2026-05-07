package io.github.xseejx.collectorframework;


// IMPORTS
import java.lang.annotation.*;
//


/**
 * Interface: CollectorMetadata
 * An Interface used to build metadata so that it is easier to get informations of a Collector
 * 
 * Metadata informations:
 * -name                    -> name
 * -description             -> ""
 * -tags                    -> {group, specific}
 * -threadSafe              -> true/false (e.g will collector run into resource lock failures?)
 * -CollectorParameter[]    -> parameters = [{
                                                    @CollectorParameter(
                                                        key = "recursive",
                                                        type = ParameterType.BOOLEAN,
                                                        defaultValue = "true"
                                                    ),
                                                    @CollectorParameter(
                                                        key = "path",
                                                        type = ParameterType.PATH,
                                                        required = true
                                                    )
                                                }]
 */


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

    /**
     * Types of avaible parameters
     */
    public enum ParameterType {
        STRING,
        BOOLEAN,
        INTEGER,
        FLOAT,
        LONG,
        PATH,
        BINARY,
        UNKNOWN
    }
}
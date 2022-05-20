package com.softawii.curupira.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a group.
 *
 * <p>
 *     The annotation is not related to JDA, it's the way to track groups internally of the library and create the help command.
 * </p>
 *
 * @author yaansz
 * @version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IGroup {
    String name()        default "";
    String description() default "";
}

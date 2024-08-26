package com.softawii.curupira.v2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// TODO: v2.1 specify target variable (like), Foo Variable1 has one function, Foo Variable2 has another function
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordAutoComplete {
    String name() default "";
}

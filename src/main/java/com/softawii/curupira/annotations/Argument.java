package com.softawii.curupira.annotations;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Arguments.class)
public @interface Argument {
    String name()        default "";
    String description() default "";
    boolean required() default false;
    OptionType type()    default OptionType.STRING;
}

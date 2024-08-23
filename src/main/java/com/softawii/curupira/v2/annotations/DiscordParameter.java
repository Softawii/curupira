package com.softawii.curupira.v2.annotations;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordParameter {

    String name() default "";
    String description() default "";
    boolean required() default true;
    OptionType type() default OptionType.UNKNOWN;
    boolean autoComplete() default false;
    // TODO: Add choice support
}
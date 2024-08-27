package com.softawii.curupira.v2.annotations.commands;

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
    DiscordChoice[] choices() default {};
    // TODO: V2.1 Add range support (arg1, arg2, arg3, etc.)
}

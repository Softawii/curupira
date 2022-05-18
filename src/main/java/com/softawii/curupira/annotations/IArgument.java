package com.softawii.curupira.annotations;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(IArguments.class)
public @interface IArgument {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Choice {
        String key();
        String value() default "";
    }
    String name()             default "";
    String description()      default "";
    boolean required()        default false;
    OptionType type()         default OptionType.STRING;
    boolean hasAutoComplete() default false;
    Choice[] choices()        default {};
}

package com.softawii.curupira.annotations;

import com.softawii.curupira.core.CommandHandler;
import kotlin.Pair;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.*;
import java.util.Map;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Arguments.class)
public @interface Argument {
    String name()             default "";
    String description()      default "";
    boolean required()        default false;
    OptionType type()         default OptionType.STRING;
    boolean hasAutoComplete() default false;
    Choice[] choices()        default {};
}

package com.softawii.curupira.annotations;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.*;

/**
 * Annotation for the argument of a command.
 * <p>
 *     The annotation is used to define the argument of a command.
 *     It will be used by the {@link com.softawii.curupira.annotations.ICommand} class to
 *     create the command.
 *
 *     The {@link com.softawii.curupira.annotations.ICommand} class will be detected by
 *     {@link com.softawii.curupira.core.Curupira} class and will be tracked by OnSlashCommandInteraction.
 * </p>
 *
 * @version 1.0.0
 * @author yaansz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(IArguments.class)
public @interface IArgument {

    /**
     * This is a choice of the argument.
     *
     * <p>
     *     It is used to define if we will recommend you some possibilities, if {@link com.softawii.curupira.annotations.IArguments}.hasAutoComplete
     *     else, it can be used to define specific options for the argument.
     * </p>
     *
     * @version 1.0.0
     * @author yaansz
     */
    @Retention(RetentionPolicy.RUNTIME)
    @interface IChoice {
        String key();
        String value() default "";
    }
    String name()             default "";
    String description()      default "";
    boolean required()        default false;
    OptionType type()         default OptionType.STRING;
    boolean hasAutoComplete() default false;
    IChoice[] choices()        default {};
}

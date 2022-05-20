package com.softawii.curupira.annotations;

import com.softawii.curupira.properties.Environment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     Annotation to track dynamically {@link net.dv8tion.jda.internal.interactions.CommandDataImpl}.
 *     It is used to track the command name, description and type
 *
 * </p>
 *
 * <p>
 *     Posteriorly it is used to create the class CommandHandler and track the commands
 *     inside the {@link com.softawii.curupira.core.Curupira}
 * </p>
 *
 * <p>
 *     The method can have the following parameters:
 *     {@link net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent} or
 *     {@link net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent} or
 *     {@link net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent}
 * </p>
 *
 * @author yaansz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ICommand {

    String       name()        default "";
    String       description() default "";
    Environment  environment() default Environment.SERVER;
    Permission[] permissions() default {};
    Type         type()        default Type.SLASH;

}

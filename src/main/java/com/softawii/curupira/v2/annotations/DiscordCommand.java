package com.softawii.curupira.v2.annotations;

import com.softawii.curupira.v2.enums.DiscordEnvironment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordCommand {
    String name() default "";
    String description() default "";
    boolean ephemeral() default false;
    DiscordEnvironment environment() default DiscordEnvironment.SERVER;
    Permission[] permissions() default {};
    Command.Type type() default Command.Type.SLASH;
}

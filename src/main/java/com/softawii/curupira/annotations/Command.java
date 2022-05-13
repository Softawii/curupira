package com.softawii.curupira.annotations;

import com.softawii.curupira.properties.Environment;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.dv8tion.jda.api.interactions.commands.Command.Type;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String       name()        default "";
    String       description() default "";
    Environment  environment() default Environment.SERVER;
    Permission[] permissions();
    Type         type()        default Type.SLASH;

}

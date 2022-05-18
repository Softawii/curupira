package com.softawii.curupira.annotations;

import com.softawii.curupira.properties.Environment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IModal {

    @interface ITextInput {

        String id();
        String label();
        TextInputStyle style();
        String placeholder() default "";
        int minLength() default -1;
        int maxLength() default -1;
        boolean required() default true;
    }

    String id();
    String description() default "";
    String title();
    Environment environment() default Environment.SERVER;
    Permission[] permissions() default {};
    ITextInput[] textInputs() default {};
    Command.Type generate() default Command.Type.UNKNOWN;
}

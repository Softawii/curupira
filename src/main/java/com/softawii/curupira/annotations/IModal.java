package com.softawii.curupira.annotations;

import com.softawii.curupira.properties.Environment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 *     Annotation to track dynamically {@link net.dv8tion.jda.internal.interactions.ModalInteractionImpl}.
 *     It is used to track the modal id, title and {@link net.dv8tion.jda.api.interactions.components.text.TextInput}
 * </p>
 *
 * <p>
 *     Parameters as permission, environment and command will be used to create the CommandHandler
 *     if generate is different from UNKNOWN.
 * </p>
 *
 * @author yaansz
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IModal {

    /**
     * <p>
     *     Annotation to track dynamically {@link net.dv8tion.jda.internal.interactions.ModalInteractionImpl}.
     *     It is used to track the modal id, title and {@link net.dv8tion.jda.api.interactions.components.text.TextInput}
     * </p>
     *
     * @author yaansz
     */
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
    String title() default "";
    Environment environment() default Environment.SERVER;
    Permission[] permissions() default {};
    ITextInput[] textInputs() default {};
    Command.Type generate() default Command.Type.UNKNOWN;
}

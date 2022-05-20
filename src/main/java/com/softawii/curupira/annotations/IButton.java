package com.softawii.curupira.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for the button.
 *
 * <p>
 *     The annotation will be used to track the method as a button, the OnButtonInteractionEvent will call it automatically,
 *     the method needs to have the first and unique parameter as a {@link net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent}.
 * </p>
 *
 * @version 1.0.0
 * @author yaansz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IButton {
    String id() default "";
}

package com.softawii.curupira.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 *      It is used to track a method as a menu, the method needs to have the
 *      first and unique parameter as a {@link net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent}.
 * </p>
 *
 * @author yaansz
 * @version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IMenu {
    String id();
}

package com.softawii.curupira.v2.annotations;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordController {
    // Controller properties
    String value() default "";
    String parent() default "";
    String description() default "";
    boolean hidden() default false;
    // I8n properties
    String resource() default "";
    DiscordLocale[] locales() default {};

}

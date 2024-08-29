package com.softawii.curupira.v2.annotations;

import com.softawii.curupira.v2.enums.DiscordEnvironment;
import net.dv8tion.jda.api.Permission;
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
    // Protection properties
    // TODO: V2.1 Owners Only
    Permission[] permissions() default {};
    DiscordEnvironment environment() default DiscordEnvironment.SERVER;
    // I8n properties
    String resource() default "";
    DiscordLocale[] locales() default {};
    DiscordLocale defaultLocale() default DiscordLocale.ENGLISH_US;

}

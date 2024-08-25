package com.softawii.curupira.example.exceptions;

import com.softawii.curupira.v2.annotations.DiscordException;
import com.softawii.curupira.v2.annotations.DiscordExceptions;
import com.softawii.curupira.v2.annotations.LocaleType;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

@DiscordExceptions(packages = "com.softawii.curupira.example")
public class FooExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FooExceptionHandler.class);

    @DiscordException(NullPointerException.class)
    public void handle(Throwable exception, Interaction interaction, LocalizationManager localization, @LocaleType DiscordLocale locale) {
        LOGGER.error("An error occurred 2, info,  exception: {}, user id: {}, user name: {}", exception, interaction.getUser().getIdLong(), interaction.getUser().getName());

        if(interaction instanceof GenericCommandInteractionEvent event) {
            event.reply(localization.getLocalizedString("foo.bar.nullpointer", locale)).queue();
        }

    }
}
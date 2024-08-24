package com.softawii.curupira.example.exceptions;

import com.softawii.curupira.v2.annotations.DiscordException;
import com.softawii.curupira.v2.annotations.DiscordExceptions;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

@DiscordExceptions
public class ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    @DiscordException(NullPointerException.class)
    public void handle(NullPointerException exception, Interaction interaction) {
        LOGGER.error("An error occurred 1", exception);
    }

    @DiscordException(InvocationTargetException.class)
    public void handle(InvocationTargetException exception, Interaction interaction) {
        LOGGER.error("An error occurred 2, info,  exception: {}, user id: {}, user name: {}", exception.getTargetException(), interaction.getUser().getIdLong(), interaction.getUser().getName());

        if(interaction instanceof GenericCommandInteractionEvent event) {
            event.reply(exception.getTargetException().getMessage()).queue();
        }

    }
}
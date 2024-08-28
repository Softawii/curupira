package com.softawii.curupira.example.exceptions;

import com.softawii.curupira.v2.annotations.DiscordException;
import com.softawii.curupira.v2.annotations.DiscordExceptions;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordExceptions
public class GenericExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionHandler.class);

    @DiscordException(Throwable.class)
    public void onThrowable(Throwable throwable, Interaction interaction) {
        logger.error("An error occurred", throwable);
        if(interaction instanceof GenericCommandInteractionEvent event)  {
            event.reply("An error occurred: " + throwable.getMessage()).setEphemeral(true).queue();
        }
        // TODO: Check every interaction type
    }
}

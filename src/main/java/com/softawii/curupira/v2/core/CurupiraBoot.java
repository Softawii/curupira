package com.softawii.curupira.v2.core;

import com.softawii.curupira.v2.integration.ContextProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurupiraBoot extends ListenerAdapter {

    private final Logger logger;

    private final JDA jda;

    private final ExceptionMapper exceptionMapper;
    private final InteractionMapper mapper;

    public CurupiraBoot(@NotNull JDA jda, @NotNull ContextProvider context, boolean registerCommandsToDiscord, String ... packages) {
        this.logger = LoggerFactory.getLogger(CurupiraBoot.class);
        this.logger.info("Curupira is booting up...");

        this.jda = jda;
        this.logger.info("Curupira configuration loaded. Values: jda={}, registerCommandsToDiscord={}, packages={}", jda, registerCommandsToDiscord, packages);

        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // reset commands?
        if(registerCommandsToDiscord) {
            this.jda.updateCommands().addCommands().queue();
            for(Guild guild : this.jda.getGuilds()) {
                guild.updateCommands().addCommands().queue();
            }
        }

        this.exceptionMapper = new ExceptionMapper(context, packages);
        this.mapper = new InteractionMapper(jda, context, exceptionMapper, registerCommandsToDiscord, packages);

        this.jda.addEventListener(this);
        this.logger.info("Curupira is ready!");
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        this.logger.info("Command interaction received. Event: {}", event);
        this.mapper.onCommandInteractionReceived(event);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        this.logger.info("Modal interaction received. Event: {}", event);
        this.mapper.onGenericInteractionCreateEvent(event.getModalId(), event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        this.logger.info("Button interaction received. Event: {}", event);
        this.mapper.onGenericInteractionCreateEvent(event.getComponentId(), event);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        this.logger.info("String select interaction received. Event: {}", event);
        this.mapper.onGenericInteractionCreateEvent(event.getComponentId(), event);
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        this.logger.info("Entity select interaction received. Event: {}", event);
        this.mapper.onGenericInteractionCreateEvent(event.getComponentId(), event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        this.logger.info("Command autocomplete interaction received. Event: {}", event);
        this.mapper.onAutoComplete(event);
    }
}

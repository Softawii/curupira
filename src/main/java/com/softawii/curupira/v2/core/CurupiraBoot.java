package com.softawii.curupira.v2.core;

import com.softawii.curupira.v2.core.command.CommandMapper;
import com.softawii.curupira.v2.core.exception.ExceptionMapper;
import com.softawii.curupira.v2.integration.ContextProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurupiraBoot extends ListenerAdapter {

    private final Logger logger;

    private final JDA jda;

    private final ExceptionMapper exceptionMapper;
    private final CommandMapper mapper;

    public CurupiraBoot(@NotNull JDA jda, @NotNull ContextProvider context, boolean registerCommandsToDiscord, String ... packages) {
        this.logger = LoggerFactory.getLogger(CurupiraBoot.class);
        this.logger.info("Curupira is booting up...");

        this.jda = jda;
        this.logger.info("Curupira configuration loaded. Values: jda={}, registerCommandsToDiscord={}, packages={}", jda, registerCommandsToDiscord, packages);

        // reset commands?
        if(registerCommandsToDiscord) {
            this.jda.updateCommands().addCommands().queue();
        }

        this.exceptionMapper = new ExceptionMapper(context, packages);
        this.mapper = new CommandMapper(jda, context, exceptionMapper, registerCommandsToDiscord, packages);

        this.jda.addEventListener(this);
        this.logger.info("Curupira is ready!");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        this.logger.debug("Slash command interaction received. Event: {}", event);
        this.mapper.onCommandInteractionReceived(event);
    }

}

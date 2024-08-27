package com.softawii.curupira.v2.core.handler;

import com.softawii.curupira.v2.annotations.*;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import com.softawii.curupira.v2.api.exception.MissingPermissionsException;
import com.softawii.curupira.v2.enums.DiscordEnvironment;
import com.softawii.curupira.v2.localization.LocalizationManager;
import com.softawii.curupira.v2.parser.DiscordToJavaParser;
import com.softawii.curupira.v2.parser.JavaToDiscordParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class InteractionHandler {
    private static final ChannelType[] PRIVATE_CHANNELS = {
            ChannelType.PRIVATE,
            ChannelType.GROUP
    };

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final JDA jda;
    private final Object instance;
    private final Method interaction;
    // i18n
    private final LocalizationManager localization;
    private final DiscordEnvironment environment;
    private final String id;
    private boolean ephemeral; // TODO: check it

    public InteractionHandler(JDA jda, Object instance, Method interaction, LocalizationFunction localization, DiscordLocale defaultLocale, DiscordEnvironment environment, String id) {
        this.jda = jda;
        this.interaction = interaction;
        this.instance = instance;
        this.localization = new LocalizationManager(localization, defaultLocale);
        this.environment = environment;
        this.id = id;
    }

    public Class<?> getControllerClass() {
        return interaction.getDeclaringClass();
    }

    public LocalizationManager getLocalization() {
        return localization;
    }

    public String getFullCommandName() {
        return id;
    }

    private void register() {
        // getting the options
        // TODO: this.ephemeral = commandInfo.ephemeral();
    }

    private Object[] getParameters(Interaction event, Method target) {
        List<Object> parameters = new ArrayList<>();

        for(Parameter parameter : target.getParameters())
            parameters.add(DiscordToJavaParser.getParameterFromEvent(event, parameter, localization));
        return parameters.toArray();
    }

    public void execute(GenericInteractionCreateEvent event) throws InvocationTargetException, IllegalAccessException {
        // Guild Only: Discord don't allow to execute commands in DMs
        // Both: no need to check if the command is available in the guild
        // Private Only: Discord don't check this
        if(environment == DiscordEnvironment.PRIVATE && !List.of(PRIVATE_CHANNELS).contains(event.getChannelType())) {
            throw new MissingPermissionsException();
        }

        Object result = interaction.invoke(instance, getParameters(event, interaction));
        // something to reply
        if(result != null) {
            if(result instanceof TextLocaleResponse response) {
                result = localization.getLocalizedString(response.code(), event.getUserLocale(), response.args());
            }

            if(event instanceof GenericComponentInteractionCreateEvent interactionEvent) {
                JavaToDiscordParser.responseFromCommandEvent(interactionEvent, result, ephemeral);
            } else if(event instanceof ModalInteraction modalEvent) {
                JavaToDiscordParser.responseFromCommandEvent(modalEvent, result, ephemeral);
            }
        }
    }

}
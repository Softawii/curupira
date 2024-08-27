package com.softawii.curupira.v2.core.handler;

import com.softawii.curupira.v2.annotations.*;
import com.softawii.curupira.v2.annotations.commands.DiscordAutoComplete;
import com.softawii.curupira.v2.annotations.commands.DiscordChoice;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import com.softawii.curupira.v2.api.exception.MissingPermissionsException;
import com.softawii.curupira.v2.enums.DiscordEnvironment;
import com.softawii.curupira.v2.localization.LocalizationManager;
import com.softawii.curupira.v2.parser.DiscordToJavaParser;
import com.softawii.curupira.v2.parser.JavaToDiscordParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {
    private static final ChannelType[] PRIVATE_CHANNELS = {
            ChannelType.PRIVATE,
            ChannelType.GROUP
    };

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final JDA jda;
    private final Object instance;
    private final Method command;
    private Map<String, Method> autoComplete;
    // i18n
    private final LocalizationManager localization;
    private final DiscordEnvironment environment;

    private List<OptionData> options;
    private boolean ephemeral;

    public CommandHandler(JDA jda, Object instance, Method command, LocalizationFunction localization, DiscordLocale defaultLocale, DiscordEnvironment environment) {
        this.jda = jda;
        this.command = command;
        this.instance = instance;
        this.localization = new LocalizationManager(localization, defaultLocale);
        this.environment = environment;
        this.autoComplete = new HashMap<>();

        register();
    }

    public Class<?> getControllerClass() {
        return command.getDeclaringClass();
    }

    public List<OptionData> getOptions() {
        return options;
    }

    public LocalizationManager getLocalization() {
        return localization;
    }

    public void addAutoComplete(Method autoComplete, DiscordAutoComplete autoCompleteInfo) {
        if(this.autoComplete.containsKey(autoCompleteInfo.variable())) {
            throw new RuntimeException("AutoComplete method already set");
        }
        this.autoComplete.put(autoCompleteInfo.variable(), autoComplete);
    }

    public String getFullCommandName() {
        DiscordController controllerInfo = command.getDeclaringClass().getAnnotation(DiscordController.class);
        DiscordCommand commandInfo = command.getAnnotation(DiscordCommand.class);

        if(controllerInfo.hidden()) {
            return commandInfo.name();
        } else if(controllerInfo.parent().isBlank()) {
            return controllerInfo.value() + " " + commandInfo.name();
        } else {
            return controllerInfo.parent() + " " + controllerInfo.value() + " " + commandInfo.name();
        }
    }

    private void register() {
        DiscordCommand commandInfo = command.getAnnotation(DiscordCommand.class);

        if(commandInfo == null) {
            throw new RuntimeException("Method " + command.getName() + " is missing the DiscordCommand annotation.");
        }

        // Register the command to Discord
        String name = commandInfo.name();

        // getting the options
        this.ephemeral = commandInfo.ephemeral();
        this.options = mapOptions();

        // Log
        logger.info("Registering command: {}", name);
    }

    private List<OptionData> mapOptions() {
        List<OptionData> options = new ArrayList<>();

        for(Parameter parameter : command.getParameters()) {
            DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
            if(annotation == null) {
                continue;
            }

            OptionType type = annotation.type();

            if(type == OptionType.UNKNOWN)
                type = JavaToDiscordParser.getTypeFromClass(parameter.getType());

            OptionData option = new OptionData(type, annotation.name(), annotation.description(), annotation.required(), annotation.autoComplete());
            if(annotation.choices().length > 0) {
                option.addChoices(mapChoices(annotation.choices(), type));
            }
            options.add(option);

            logger.info("Found parameter: name: {}, type: {}, required: {}, autocomplete: {}", annotation.name(), type, annotation.required(), annotation.autoComplete());
        }

        return options;
    }

    private List<Command.Choice> mapChoices(DiscordChoice[] choices, OptionType type) {
        // Long, Double, String
        ArrayList<Command.Choice> result = new ArrayList<>();
        for(DiscordChoice choice : choices) {
            String key = choice.name();
            String value = choice.value().isBlank() ? key : choice.name();

            if(type == OptionType.STRING) {
                result.add(new Command.Choice(key, value));
            } else if(type == OptionType.INTEGER) {
                result.add(new Command.Choice(key, Integer.parseInt(value)));
            } else if(type == OptionType.NUMBER) {
                result.add(new Command.Choice(key, Double.parseDouble(value)));
            } else {
                throw new RuntimeException("OptionType not supported");
            }
        }

        return result;
    }

    private Object[] getParameters(Interaction event, Method target) {
        List<Object> parameters = new ArrayList<>();

        for(Parameter parameter : target.getParameters())
            parameters.add(DiscordToJavaParser.getParameterFromEvent(event, parameter, localization));
        return parameters.toArray();
    }

    public void execute(GenericCommandInteractionEvent event) throws InvocationTargetException, IllegalAccessException {
        // Guild Only: Discord don't allow to execute commands in DMs
        // Both: no need to check if the command is available in the guild
        // Private Only: Discord don't check this
        if(environment == DiscordEnvironment.PRIVATE && !List.of(PRIVATE_CHANNELS).contains(event.getChannelType())) {
            throw new MissingPermissionsException();
        }

        Object result = command.invoke(instance, getParameters(event, command));
        // something to reply
        if(result != null) {
            if(result instanceof TextLocaleResponse response) {
                result = localization.getLocalizedString(response.code(), event.getUserLocale(), response.args());
            }
            JavaToDiscordParser.responseFromCommandEvent(event, result, ephemeral);
        }
    }

    public void autoComplete(CommandAutoCompleteInteractionEvent event) throws InvocationTargetException, IllegalAccessException {
        if(autoComplete.isEmpty()) {
            throw new RuntimeException("AutoComplete method not set");
        }

        String variable = event.getFocusedOption().getName();

        Method defaultHandler = autoComplete.getOrDefault("", null);
        Method finalHandler = this.autoComplete.getOrDefault(variable, defaultHandler);

        if(finalHandler == null) {
            throw new RuntimeException("AutoComplete method not found");
        }

        Command.Choice[] choices = (Command.Choice[]) finalHandler.invoke(instance, getParameters(event, finalHandler));
        event.replyChoices(choices).queue();
    }
}

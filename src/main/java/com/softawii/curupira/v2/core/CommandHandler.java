package com.softawii.curupira.v2.core;

import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.DiscordParameter;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import com.softawii.curupira.v2.parser.JavaToDiscordParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.internal.interactions.command.localization.LocalizationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.softawii.curupira.v2.parser.DiscordToJavaParser.getParameterFromEvent;

class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final JDA jda;
    private final Object instance;
    private final Method method;
    // i18n
    private final LocalizationFunction localization;
    private final DiscordLocale defaultLocale;

    private List<OptionData> options;
    private boolean ephemeral;

    public CommandHandler(JDA jda, Object instance, Method method, LocalizationFunction localization, DiscordLocale defaultLocale) {
        this.jda = jda;
        this.method = method;
        this.instance = instance;
        this.localization = localization;
        this.defaultLocale = defaultLocale;

        register();
    }

    public List<OptionData> getOptions() {
        return options;
    }

    public String getFullCommandName() {
        DiscordController controllerInfo = method.getDeclaringClass().getAnnotation(DiscordController.class);
        DiscordCommand commandInfo = method.getAnnotation(DiscordCommand.class);

        if(controllerInfo.hidden()) {
            return commandInfo.name();
        } else if(controllerInfo.parent().isBlank()) {
            return controllerInfo.value() + " " + commandInfo.name();
        } else {
            return controllerInfo.parent() + " " + controllerInfo.value() + " " + commandInfo.name();
        }
    }

    private void register() {
        DiscordCommand commandInfo = method.getAnnotation(DiscordCommand.class);

        if(commandInfo == null) {
            throw new RuntimeException("Method " + method.getName() + " is missing the DiscordCommand annotation.");
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

        for(Parameter parameter : method.getParameters()) {
            DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
            if(annotation == null) {
                continue;
            }

            OptionType type = annotation.type();

            if(type == OptionType.UNKNOWN)
                type = JavaToDiscordParser.getTypeFromClass(parameter.getType());

            OptionData option = new OptionData(type, annotation.name(), annotation.description(), annotation.required(), annotation.autoComplete());
            options.add(option);

            logger.info("Found parameter: name: {}, type: {}, required: {}, autocomplete: {}", annotation.name(), type, annotation.required(), annotation.autoComplete());
        }

        return options;
    }

    private Object[] getParameters(CommandInteractionPayload event) {
        List<Object> parameters = new ArrayList<>();

        for(Parameter parameter : method.getParameters())
            parameters.add(getParameterFromEvent(event, parameter));
        return parameters.toArray();
    }

    public void execute(GenericCommandInteractionEvent event) throws InvocationTargetException, IllegalAccessException {
        Object result = method.invoke(instance, getParameters(event));
        // something to reply
        if(result != null) {
            if(result instanceof TextLocaleResponse response) {
                Map<DiscordLocale, String> locales = localization.apply(response.code());
                String mask;
                if(locales.containsKey(event.getUserLocale())) {
                    mask = locales.get(event.getUserLocale());
                } else {
                    mask = locales.get(defaultLocale);
                }

                result = String.format(mask, response.args());
            }

            JavaToDiscordParser.responseFromCommandEvent(event, result, ephemeral);
        }
    }
}

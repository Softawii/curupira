package com.softawii.curupira.v2.core;

import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.DiscordParameter;
import com.softawii.curupira.v2.parser.DiscordToJavaParser;
import com.softawii.curupira.v2.parser.JavaToDiscordParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.softawii.curupira.v2.parser.DiscordToJavaParser.getParameterFromEvent;

class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private JDA jda;
    private Object instance;
    private Method method;
    private Boolean registerToDiscord;
    private CommandDataImpl commandInfo;
    private SubcommandData subcommandData;
    private String fullCommandName;

    public CommandHandler(JDA jda, Object instance, Method method, Boolean registerToDiscord) {
        this.jda = jda;
        this.method = method;
        this.instance = instance;
        this.registerToDiscord= registerToDiscord;
        this.fullCommandName = defineFullCommandName();

        register();
    }

    public CommandDataImpl getCommandInfo() {
        return commandInfo;
    }

    public SubcommandData getSubcommandData() {
        return subcommandData;
    }

    public String getFullCommandName() {
        return fullCommandName;
    }

    private String defineFullCommandName() {
        DiscordController controllerInfo = method.getDeclaringClass().getAnnotation(DiscordController.class);
        DiscordCommand commandInfo = method.getAnnotation(DiscordCommand.class);
        return controllerInfo.value() + " " + commandInfo.name();
    }

    private void register() {
        DiscordCommand commandInfo = method.getAnnotation(DiscordCommand.class);

        if(commandInfo == null) {
            throw new RuntimeException("Method " + method.getName() + " is missing the DiscordCommand annotation.");
        }

        // Register the command to Discord
        String name = commandInfo.name();

        // getting the options
        List<OptionData> options = getOptions();
        String optionsString = options.stream()
                .map(option -> option.getName() + " (" + option.getType() + ")")
                .reduce("", (a, b) -> a + ", " + b);

        // case 1: creating the command data
        CommandDataImpl commandData = new CommandDataImpl(name, commandInfo.description());
        commandData.addOptions(options);

        // case 2: creating the subcommand data
        SubcommandData subcommandData = new SubcommandData(name, commandInfo.description());
        subcommandData.addOptions(options);

        // Log
        logger.info("Registering command: {}, parameters: {}", name, optionsString);

        this.commandInfo = commandData;
        this.subcommandData = subcommandData;
    }

    private List<OptionData> getOptions() {
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

    public void handle(SlashCommandInteractionEvent event) throws InvocationTargetException, IllegalAccessException {
        method.invoke(instance, getParameters(event));
    }
}

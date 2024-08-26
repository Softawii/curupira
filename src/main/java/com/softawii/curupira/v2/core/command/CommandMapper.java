package com.softawii.curupira.v2.core.command;

import com.softawii.curupira.v2.annotations.DiscordAutoComplete;
import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.core.exception.ExceptionMapper;
import com.softawii.curupira.v2.enums.DiscordEnvironment;
import com.softawii.curupira.v2.integration.ContextProvider;
import com.softawii.curupira.v2.utils.ScanUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommandMapper {

    private final Logger logger;
    private final JDA jda;
    private final ContextProvider context;
    private final boolean registerCommandsToDiscord;
    private final String[] packages;

    private final ExceptionMapper exceptionMapper;
    private final Map<String, CommandHandler> commands;
    private final Map<String, CommandDataImpl> data;

    public CommandMapper(JDA jda, ContextProvider context, ExceptionMapper exceptionMapper, boolean registerCommandsToDiscord, String ... packages) {
        this.logger = LoggerFactory.getLogger(CommandMapper.class);
        this.jda = jda;
        this.context = context;
        this.registerCommandsToDiscord = registerCommandsToDiscord;
        this.packages = packages;
        this.exceptionMapper = exceptionMapper;

        this.commands = new HashMap<>();
        this.data = new HashMap<>();

        scan();
        apply();
    }

    private void apply() {
        if(registerCommandsToDiscord) {
            for(Map.Entry<String, CommandDataImpl> entry : data.entrySet()) {
                this.logger.info("Apply command: {}", entry.getValue());
                jda.upsertCommand(entry.getValue()).queue();
            }
        }
    }

    public void scan() {
        for (String packageName : packages) {
            scanPackage(packageName);
        }
    }

    private void scanPackage(String packageName) {
        List<Class> classes = ScanUtils.getClassesInPackage(packageName).stream().filter(clazz -> clazz.isAnnotationPresent(DiscordController.class)).toList();

        for(Class clazz : classes) {
            Object instance = context.getInstance(clazz);
            this.logger.info("Found controller: {}, instance: {}", clazz.getName(), instance);
            scanClass(clazz);
        }
    }

    private LocalizationFunction getLocalizationFunction(DiscordController controllerInfo) {
        if (controllerInfo.resource().isBlank()) return null;

        DiscordLocale[] locales = getLocales(controllerInfo);
        return ResourceBundleLocalizationFunction.fromBundles(controllerInfo.resource(), locales).build();
    }

    private DiscordLocale[] getLocales(DiscordController controllerInfo) {
        List<DiscordLocale> locales = new ArrayList<>();
        locales.add(controllerInfo.defaultLocale());
        locales.addAll(Arrays.asList(controllerInfo.locales()));

        DiscordLocale[] localesArray = new DiscordLocale[locales.size()];
        locales.toArray(localesArray);
        return localesArray;
    }

    private void scanClass(Class clazz) {
        findCommands(clazz);
        findAutoComplete(clazz);
    }

    private void findCommands(Class clazz) {
        List<Method> methods = ScanUtils.getMethodsAnnotatedWith(clazz, DiscordCommand.class);
        DiscordController controllerInfo = (DiscordController) clazz.getAnnotation(DiscordController.class);
        LocalizationFunction localization = getLocalizationFunction(controllerInfo);
        DiscordLocale defaultLocale = controllerInfo.defaultLocale();

        if(controllerInfo.hidden() && !controllerInfo.parent().isBlank())
            throw new RuntimeException("Controller cannot be hidden and have a parent at the same time");

        if(!controllerInfo.hidden() && controllerInfo.value().isBlank())
            throw new RuntimeException("Controller must have a value if it's not hidden");

        this.logger.info("Registering controller: {}", controllerInfo.value());

        for(Method method : methods) {
            this.logger.info("Found method: {}", method.getName());
            CommandHandler handler = scanMethod(method, localization, defaultLocale, controllerInfo.environment());
            registerCommand(handler, controllerInfo, method.getAnnotation(DiscordCommand.class), localization);
        }
    }

    private void findAutoComplete(Class clazz) {
        List<Method> methods = ScanUtils.getMethodsAnnotatedWith(clazz, DiscordAutoComplete.class);
        DiscordController controllerInfo = (DiscordController) clazz.getAnnotation(DiscordController.class);

        this.logger.info("findAutoComplete: Autocomplete to controller: {}", controllerInfo.value());

        for(Method method : methods) {
            this.logger.info("findAutoComplete: Found method: {}", method.getName());
            // get key from method name
            DiscordAutoComplete autoComplete = method.getAnnotation(DiscordAutoComplete.class);
            String key = getAutoCompleteKey(controllerInfo, autoComplete);
            if(!this.commands.containsKey(key)) {
                this.logger.error("findAutoComplete: Command not found: {}", key);
                throw new RuntimeException("Command not found to set autocomplete: " + key);
            }
            this.logger.info("findAutoComplete: Registering autocomplete to command: {}, variable: {}", key, autoComplete.variable());
            this.commands.get(key).addAutoComplete(method, autoComplete);
        }
    }

    private String getAutoCompleteKey(DiscordController controllerInfo, DiscordAutoComplete autoComplete) {
        if(controllerInfo.hidden()) {
            return autoComplete.name();
        } else if(controllerInfo.parent().isBlank()) {
            return controllerInfo.value() + " " + autoComplete.name();
        } else {
            return controllerInfo.parent() + " " + controllerInfo.value() + " " + autoComplete.name();
        }
    }

    private CommandHandler scanMethod(Method method, LocalizationFunction localization, DiscordLocale defaultLocale, DiscordEnvironment environment) {
        return new CommandHandler(jda, context.getInstance(method.getDeclaringClass()), method, localization, defaultLocale, environment);
    }

    private void registerCommand(CommandHandler handler, DiscordController controllerInfo, DiscordCommand commandInfo, LocalizationFunction localization) {
        this.logger.info("Registering command: {}", handler.getFullCommandName());
        String[] name = handler.getFullCommandName().split(" ");

        this.commands.put(handler.getFullCommandName(), handler);

        if(!this.data.containsKey(name[0])) {
            CommandDataImpl commandData = new CommandDataImpl(name[0], controllerInfo.description());
            commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(controllerInfo.permissions()));
            commandData.setGuildOnly(controllerInfo.environment() == DiscordEnvironment.SERVER);
            if(localization != null) commandData.setLocalizationFunction(localization);
            this.data.put(name[0], commandData);
        }

        // only 1 level of subcommands /foo
        if(name.length == 1) {
            this.data.get(name[0]).addOptions(handler.getOptions());
        }
        // 2 levels of subcommands /foo bar
        else if(name.length == 2) {
            SubcommandData subcommandData = new SubcommandData(name[1], commandInfo.description());
            subcommandData.addOptions(handler.getOptions());
            this.data.get(name[0]).addSubcommands(subcommandData);
        }
        // 3 levels of subcommands /foo bar baz
        else if (name.length == 3) {
            this.data.get(name[0]).getSubcommandGroups().stream().filter(group -> group.getName().equals(name[1])).findFirst().ifPresentOrElse(group -> {
                SubcommandData subcommandData = new SubcommandData(name[2], commandInfo.description());
                subcommandData.addOptions(handler.getOptions());
                group.addSubcommands(subcommandData);
            }, () -> {
                SubcommandGroupData groupData = new SubcommandGroupData(name[1], controllerInfo.description());
                SubcommandData subcommandData = new SubcommandData(name[2], commandInfo.description());
                subcommandData.addOptions(handler.getOptions());
                groupData.addSubcommands(subcommandData);
                this.data.get(name[0]).addSubcommandGroups(groupData);
            });
        }
    }

    public void onCommandInteractionReceived(GenericCommandInteractionEvent event) {
        if(commands.containsKey(event.getFullCommandName())) {
            CommandHandler handler = commands.get(event.getFullCommandName());
            try {
                handler.execute(event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                exceptionMapper.handle(handler.getControllerClass(), e, event, handler.getLocalization());
            }
        } else {
            this.logger.error("onCommandInteractionReceived: Command not found: {}", event.getFullCommandName());
            event.reply("Command not found").setEphemeral(true).queue();
        }
    }

    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(commands.containsKey(event.getFullCommandName())) {
            CommandHandler handler = commands.get(event.getFullCommandName());
            try {
                handler.autoComplete(event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                exceptionMapper.handle(handler.getControllerClass(), e, event, handler.getLocalization());
            }
        } else {
            this.logger.error("onAutoComplete: Command not found: {}", event.getFullCommandName());
            event.replyChoices(new Command.Choice[0]).queue();
        }
    }
}

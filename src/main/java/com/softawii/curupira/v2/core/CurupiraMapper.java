package com.softawii.curupira.v2.core;

import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.integration.ContextProvider;
import com.softawii.curupira.v2.utils.ScanUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CurupiraMapper {

    private final Logger logger;
    private final JDA jda;
    private final ContextProvider context;
    private final boolean registerCommandsToDiscord;
    private final String[] packages;

    private final Map<String, CommandHandler> commands;
    private final Map<String, CommandDataImpl> data;

    public CurupiraMapper(JDA jda, ContextProvider context, boolean registerCommandsToDiscord, String ... packages) {
        this.logger = LoggerFactory.getLogger(CurupiraMapper.class);
        this.jda = jda;
        this.context = context;
        this.registerCommandsToDiscord = registerCommandsToDiscord;
        this.packages = packages;

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

    private void scanClass(Class clazz) {
        List<Method> methods = ScanUtils.getMethodsAnnotatedWith(clazz, DiscordCommand.class);
        DiscordController controllerInfo = (DiscordController) clazz.getAnnotation(DiscordController.class);

        if(controllerInfo.hidden() && !controllerInfo.parent().isBlank())
            throw new RuntimeException("Controller cannot be hidden and have a parent at the same time");

        if(!controllerInfo.hidden() && controllerInfo.value().isBlank())
            throw new RuntimeException("Controller must have a value if it's not hidden");

        this.logger.info("Registering controller: {}", controllerInfo.value());

        for(Method method : methods) {
            this.logger.info("Found method: {}", method.getName());
            CommandHandler handler = scanMethod(method);
            registerCommand(handler, controllerInfo, method.getAnnotation(DiscordCommand.class));
        }
    }

    private CommandHandler scanMethod(Method method) {
        return new CommandHandler(jda, context.getInstance(method.getDeclaringClass()), method);
    }

    private void registerCommand(CommandHandler handler, DiscordController controllerInfo, DiscordCommand commandInfo) {
        this.logger.info("Registering command: {}", handler.getFullCommandName());
        String[] name = handler.getFullCommandName().split(" ");

        this.commands.put(handler.getFullCommandName(), handler);

        if(!this.data.containsKey(name[0])) {
            CommandDataImpl commandData = new CommandDataImpl(name[0], controllerInfo.description());
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

    public void slashCommandInteractionReceived(SlashCommandInteractionEvent event) {
        if(commands.containsKey(event.getFullCommandName())) {
            CommandHandler handler = commands.get(event.getFullCommandName());
            try {
                handler.execute(event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.logger.error("Command not found: {}", event.getFullCommandName());
            event.reply("Command not found").queue();
        }
    }
}

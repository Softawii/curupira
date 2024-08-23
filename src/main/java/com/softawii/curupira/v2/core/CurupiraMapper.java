package com.softawii.curupira.v2.core;

import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.integration.ContextProvider;
import com.softawii.curupira.v2.utils.ScanUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class CurupiraMapper {

    private final Logger logger;
    private final JDA jda;
    private final ContextProvider context;
    private final boolean registerCommandsToDiscord;
    private final String[] packages;

    private Map<String, CommandHandler> commands;

    public CurupiraMapper(JDA jda, ContextProvider context, boolean registerCommandsToDiscord, String ... packages) {
        this.logger = LoggerFactory.getLogger(CurupiraMapper.class);
        this.jda = jda;
        this.context = context;
        this.registerCommandsToDiscord = registerCommandsToDiscord;
        this.packages = packages;

        this.commands = new HashMap<>();

        scan();
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

        this.logger.info("Registering controller: {}", controllerInfo.value());
        CommandDataImpl commandData = null;
        if(!controllerInfo.hidden()) {
            commandData = new CommandDataImpl(controllerInfo.value(), controllerInfo.description());
        }

        for(Method method : methods) {
            this.logger.info("Found method: {}", method.getName());
            CommandHandler handler = scanMethod(method);

            // check to concatenate the commandData
            if(commandData != null) {
                commandData.addSubcommands(handler.getSubcommandData());
            } else {
                commandData = handler.getCommandInfo();
            }

            if(registerCommandsToDiscord) {
                jda.upsertCommand(commandData).queue();
            }

            this.commands.put(handler.getFullCommandName(), handler);
        }
    }

    private CommandHandler scanMethod(Method method) {
        return new CommandHandler(jda, context.getInstance(method.getDeclaringClass()), method, registerCommandsToDiscord);
    }

    public void slashCommandInteractionReceived(SlashCommandInteractionEvent event) {
        if(commands.containsKey(event.getFullCommandName())) {
            CommandHandler handler = commands.get(event.getFullCommandName());
            try {
                handler.handle(event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.logger.error("Command not found: {}", event.getFullCommandName());
            event.reply("Command not found").queue();
        }
    }
}

package com.softawii.curupira.core;

import com.softawii.curupira.annotations.*;
import com.softawii.curupira.properties.Environment;
import com.softawii.curupira.utils.ParserCallback;
import com.softawii.curupira.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Curupira extends ListenerAdapter {

    private static final Logger LOGGER = LogManager.getLogger(Curupira.class);

    private final JDA JDA;
    private final Map<String, CommandHandler> commandMapper;
    private final Map<String, Method> buttonMapper;
    private final Map<String, Method> menuMapper;
    private final Map<String, Method> modalMapper;
    private final Map<String, List<Choice>> autoCompleteMapper;
    private final MessageEmbed helpEmbed;

    public Curupira(@NotNull JDA JDA, String @NotNull ... packages) {
        LOGGER.info("Inicializing Curupira");
        // Init
        commandMapper       = new HashMap<>();
        buttonMapper        = new HashMap<>();
        menuMapper          = new HashMap<>();
        modalMapper         = new HashMap<>();
        autoCompleteMapper  = new HashMap<>();

        // Args
        this.JDA = JDA;
        JDA.addEventListener(this);

        for(String pkg : packages) {
            setPackage(pkg);
        }

        // Help
        helpEmbed = createHelper();
        LOGGER.info("Curupira initialized");
    }

    private void setPackage(String pkgName) {
        Set<Class> classes = Utils.getClassesInPackage(pkgName);

        // Clean
        this.JDA.updateCommands().addCommands().queue();

        // For each class in the package that is a group
        classes.stream().filter(cls -> cls.isAnnotationPresent(IGroup.class)).forEach(cls -> {
            LOGGER.debug("Found IGroup: " + cls.getSimpleName());

            addCommands(cls);
            Utils.getMethodsAnnotatedBy(cls, IButton.class, buttonMapper);
            Utils.getMethodsAnnotatedBy(cls, IMenu.class  , menuMapper);
            Utils.getMethodsAnnotatedBy(cls, IModal.class , modalMapper);
        });
    }

    private void addCommands(Class cls) {
        IGroup IGroup = (IGroup) cls.getAnnotation(IGroup.class);
        List<CommandData> commands = Arrays.stream(cls.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ICommand.class))
                .map(getMethodCommandDataFunction(IGroup))
                .collect(Collectors.toList());

        commands.forEach(command -> {
            JDA.upsertCommand(command).queue();
        });
    }

    @NotNull
    private Function<Method, CommandDataImpl> getMethodCommandDataFunction(IGroup IGroup) {
        return method -> {
            LOGGER.debug("Found ICommand: " + method.getName());

            ICommand command = method.getAnnotation(ICommand.class);

            String name              = (command.name().isBlank() ? method.getName() : command.name()).toLowerCase();
            String description       = command.description();
            Environment environment  = command.environment();
            Permission[] permissions = command.permissions();
            Type type                = command.type();

            List<OptionData> options = new ArrayList<>();

            ParserCallback callback = (key, value) -> {
                if(autoCompleteMapper.containsKey(key)) throw new RuntimeException("IArgument " + '"' + key + '"' + " already mapped");
                autoCompleteMapper.put(key, value);
            };

            // One IArgument or Multiple IArguments
            if (method.isAnnotationPresent(IArgument.class)) {
                IArgument IArgument = method.getAnnotation(IArgument.class);
                options.add(Utils.parserArgument(IArgument, name, callback));

            } else if (method.isAnnotationPresent(IArguments.class)) {
                IArgument[] IArguments = method.getAnnotation(com.softawii.curupira.annotations.IArguments.class).value();

                for (IArgument IArgument : IArguments) {
                    OptionData optionData = Utils.parserArgument(IArgument, name, callback);
                    options.add(optionData);
                }
            }

            // IRange
            if (method.isAnnotationPresent(IRange.class)) {
                IRange IRange = method.getAnnotation(IRange.class);
                options.addAll(Utils.parserRange(IRange, name, callback));
            }
            else if(method.isAnnotationPresent(IRanges.class)) {
                IRange[] IRanges = method.getAnnotation(com.softawii.curupira.annotations.IRanges.class).value();

                for(IRange IRange : IRanges) {
                    options.addAll(Utils.parserRange(IRange, name, callback));
                }
            }

            CommandDataImpl commandData;
            if(type == Type.SLASH) commandData = new CommandDataImpl(name, description);
            else                   commandData = new CommandDataImpl(type, name);
            commandData.addOptions(options);

            if(commandMapper.containsKey(name)) {
                throw LOGGER.throwing(new RuntimeException("ICommand with name: " + name + " already exists"));
            }
            commandMapper.put(name, new CommandHandler(method, permissions, environment, IGroup, name, description));

            return commandData;
        };
    }

    private MessageEmbed createHelper() {
        EmbedBuilder builder = new EmbedBuilder();
        Map<String, StringBuilder> stringBuilder = new HashMap<>();

        commandMapper.forEach((name, commandHandler) -> {
            String groupName = commandHandler.getGroup().name();

            stringBuilder.computeIfPresent(groupName, (key, localBuilder) -> {
                localBuilder.append("**" + commandHandler.getName() + "**\n");
                localBuilder.append("" + commandHandler.getDescription() + "\n\n");

                return localBuilder;
            });

            stringBuilder.computeIfAbsent(groupName, k -> {
                StringBuilder localBuilder = new StringBuilder();

                localBuilder.append(commandHandler.getGroup().description() + "\n\n");

                localBuilder.append("**" + commandHandler.getName() + "**\n");
                localBuilder.append("" + commandHandler.getDescription() + "\n\n");

                return localBuilder;
            });
        });

        builder.setTitle("ICommand List");
        builder.setDescription("Here is a list of all commands");

        stringBuilder.forEach((groupName, localBuilder) -> {
            builder.addField(groupName, localBuilder.toString(), false);
        });

        this.JDA.upsertCommand("help", "help").queue();

        return builder.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LOGGER.debug("Received Slash ICommand: " + event.getName());
        try {
            if(event.getName().equals("help") && !commandMapper.containsKey("help")) {
                event.replyEmbeds(helpEmbed).queue();
            }
            else if (commandMapper.containsKey(event.getName())) {
                this.commandMapper.get(event.getName()).execute(event);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        LOGGER.debug("Received IButton: " + id);

        if(buttonMapper.containsKey(id)) {
            try {
                this.buttonMapper.get(id).invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        LOGGER.debug("Received Select IMenu: " + event.getComponentId());

        if(menuMapper.containsKey(event.getComponentId())) {
            try {
                menuMapper.get(event.getComponentId()).invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        LOGGER.debug("Received IModal: " + event.getModalId());

        if(modalMapper.containsKey(event.getModalId())) {
            try {
                LOGGER.debug("Invoking IModal: " + event.getModalId());
                modalMapper.get(event.getModalId()).invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        LOGGER.debug("Received User Context: " + event.getName());

        try {
            if (commandMapper.containsKey(event.getName())) {
                this.commandMapper.get(event.getName()).execute(event);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        LOGGER.debug("Received Message Context: " + event.getName());

        try {
            if (commandMapper.containsKey(event.getName())) {
                this.commandMapper.get(event.getName()).execute(event);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String key = event.getName() + ":" + event.getFocusedOption().getName();

        LOGGER.debug("Received ICommand Auto Complete: " + key);

        if(autoCompleteMapper.containsKey(key)) {
            List<Choice> choices = autoCompleteMapper.get(key)
                    .stream()
                    .filter(c -> c.getName().startsWith(event.getFocusedOption().getValue()))
                    .collect(Collectors.toList());
            event.replyChoices(choices).queue();
        }
    }
}

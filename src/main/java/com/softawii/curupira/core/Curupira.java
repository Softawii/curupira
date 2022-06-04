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
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.interactions.component.TextInputImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    private final Map<String, Modal>  modals;
    private final Map<String, List<Choice>> autoCompleteMapper;
    private final boolean reset;

    public Curupira(@NotNull JDA JDA, boolean reset, String @NotNull ... packages) {
        LOGGER.info("Inicializing Curupira");
        // Init
        commandMapper       = new HashMap<>();
        buttonMapper        = new HashMap<>();
        menuMapper          = new HashMap<>();
        modalMapper         = new HashMap<>();
        autoCompleteMapper  = new HashMap<>();
        modals              = new HashMap<>();

        this.reset = reset;

        // Args
        this.JDA = JDA;
        JDA.addEventListener(this);

        // Clean
        if (this.reset) this.JDA.updateCommands().addCommands().queue();

        for(String pkg : packages) {
            setPackage(pkg);
        }

        LOGGER.info("Curupira initialized in bot " + JDA.getSelfUser().getName());
        LOGGER.info("Commands: " + String.join(", ", commandMapper.keySet()));
    }

    /**
     * @param id The id of the command
     * @return Modal that was generated in the start of the class.
     */
    public Modal.Builder getModal(String id) {
        return modals.get(id).createCopy();
    }

    /**
     * <p>
     *      This is used to register all the commands in the package.
     *      We will check for IGroup, ICommand, IMenu, IModal and
     *      will start to organize everything in the maps.
     * </p>
     * @param pkgName The package name to scan for commands.
     */
    private void setPackage(String pkgName) {
        Set<Class> classes = Utils.getClassesInPackage(pkgName);

        // For each class in the package that is a group
        List<Class> groups = classes.stream().filter(cls -> cls.isAnnotationPresent(IGroup.class)).collect(Collectors.toList());

        for(Class group : groups) {
            // We have 2 levels of hierarchy
            /*
                1. Outer class (can have commands (methods) or subcommand groups (inner classes))
                2. Inner class (can have subcommands (methods)
             */
            /*
                Examples:
                /outer inner execute
                 /\     /\    /\
                 |      |     |
                 |      |     method
                 |      inner class
                outer class

                /outer execute
                 /\     /\
                  |     |
                  |     method (when there is no inner class)
                  |
                  outer class

                /execute
                 /\
                 |
                 method (when outer class is hidden)
             */

            IGroup igroup = (IGroup) group.getAnnotation(IGroup.class);
            boolean hidden = igroup.hidden();

            // Hidden -> Creating Individual Command
            if(hidden) {
                LOGGER.info(String.format("Registering Free Commands in %s", igroup.name()));
                List<Method> methods = Arrays.stream(group.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(ICommand.class)).toList();

                // Creating Individual Commands
                for(Method method : methods) {
                    CommandDataImpl commandData = getCommandFromMethod(method);
                    // It's not a subcommand, we can add to the JDA immediately

                    // TODO: Check Method Parameters
                    ICommand icommand = method.getAnnotation(ICommand.class);
                    String key = String.format("%s", commandData.getName());
                    CommandHandler handler = new CommandHandler(method, icommand.permissions(),
                            icommand.environment(), commandData.getName(),
                            icommand.description(), null);

                    if(this.commandMapper.containsKey(key)) throw new RuntimeException(String.format("Duplicate Command: %s", key));
                    this.commandMapper.put(key, handler);

                    JDA.upsertCommand(commandData).queue();
                }

                // If it's Hidden, and we have an Inner Class, that is a group
                List<Class> innerClasses = List.of(group.getDeclaredClasses()).stream().filter(cls -> cls.isAnnotationPresent(ISubGroup.class)).toList();
                for(Class innerClass : innerClasses) {
                    // We have 2 levels of hierarchy
                    // Example: /inner execute

                    ISubGroup isubgroup = (ISubGroup) innerClass.getAnnotation(ISubGroup.class);
                    String inner_name = isubgroup.name();
                    String inner_desc = isubgroup.description();

                    LOGGER.info(String.format("Registering CommandGroup (Sub)", inner_name));

                    CommandDataImpl commandData = new CommandDataImpl(inner_name, inner_desc);

                    // Mapping the subgroup
                    methods = Arrays.stream(innerClass.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(ICommand.class)).toList();
                    for(Method method : methods) {
                        SubcommandData subcommandData = getSubcommandFromMethod(method);
                        commandData.addSubcommands(subcommandData);

                        // TODO: Check Method Parameters
                        ICommand icommand = method.getAnnotation(ICommand.class);
                        String key = String.format("%s/%s", inner_name, subcommandData.getName());
                        CommandHandler handler = new CommandHandler(method, icommand.permissions(),
                                icommand.environment(), subcommandData.getName(),
                                icommand.description(), null);

                        if(this.commandMapper.containsKey(key)) throw new RuntimeException(String.format("Duplicate Command: %s", key));
                        this.commandMapper.put(key, handler);

                    }

                    // All subcommands are added, now we can send to Discord
                    JDA.upsertCommand(commandData).queue();
                }
            }
            // Not Hidden -> Creating Group Command
            else {
                LOGGER.info(String.format("Registering Commands in %s (Group)", igroup.name()));

                String outer_name = igroup.name().toLowerCase();
                String outer_desc = igroup.description();
                CommandDataImpl commandData = new CommandDataImpl(outer_name, outer_desc);

                List<Method> methods = Arrays.stream(group.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(ICommand.class)).toList();

                // Creating SubCommands
                // Example /outer execute
                for(Method method : methods) {
                    SubcommandData subcommandData = getSubcommandFromMethod(method);
                    // Not adding to JDA yet, because it's a group command, and we need to add all subcommands to it
                    // Adding to JDA later, now we will add to the map
                    commandData.addSubcommands(subcommandData);

                    // TODO: Check Method Parameters
                    ICommand icommand = method.getAnnotation(ICommand.class);
                    String key = String.format("%s/%s", outer_name, subcommandData.getName());
                    CommandHandler handler = new CommandHandler(method, icommand.permissions(),
                                                                icommand.environment(), subcommandData.getName(),
                                                                icommand.description(), null);

                    if(this.commandMapper.containsKey(key)) throw new RuntimeException(String.format("Duplicate Command: %s", key));
                    this.commandMapper.put(key, handler);
                }

                // Subcommands are added, and about SubCommandGroup????
                List<Class> innerClasses = Arrays.stream(group.getClasses()).filter(cls -> cls.isAnnotationPresent(ISubGroup.class)).toList();

                for(Class innerClass : innerClasses) {
                    ISubGroup isubgroup = (ISubGroup) innerClass.getAnnotation(ISubGroup.class);
                    String inner_name = isubgroup.name().toLowerCase();
                    String inner_desc = isubgroup.description();

                    LOGGER.info(String.format("Registering Commands in (Group) %s (SubGroup) %s", igroup.name(), inner_name));

                    SubcommandGroupData subcommandGroupData = new SubcommandGroupData(inner_name, inner_desc);

                    methods = Arrays.stream(innerClass.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(ICommand.class)).toList();
                    // Creating SubCommands in a SubCommandGroup
                    // Example /outer inner execute
                    for(Method method : methods) {
                        SubcommandData subcommandData = getSubcommandFromMethod(method);
                        subcommandGroupData.addSubcommands(subcommandData);

                        // TODO: Check Method Parameters
                        ICommand icommand = method.getAnnotation(ICommand.class);
                        String key = String.format("%s/%s/%s", outer_name, inner_name, subcommandData.getName());
                        CommandHandler handler = new CommandHandler(method, icommand.permissions(),
                                icommand.environment(), subcommandData.getName(),
                                icommand.description(), null);

                        if(this.commandMapper.containsKey(key)) throw new RuntimeException(String.format("Duplicate Command: %s", key));
                        this.commandMapper.put(key, handler);
                    }

                    commandData.addSubcommandGroups(subcommandGroupData);
                }

                // All subcommands are added, now we can send to Discord
                JDA.upsertCommand(commandData).queue();
            }
        }
    }


    private CommandDataImpl getCommandFromMethod(Method method) {
        ICommand ICommand = method.getAnnotation(ICommand.class);

        String groupName;
        String groupDesc;

        IGroup IGroup = method.getDeclaringClass().getAnnotation(IGroup.class);
        ISubGroup ISub = method.getDeclaringClass().getAnnotation(ISubGroup.class);

        if(IGroup != null) {
            groupName = IGroup.name().toLowerCase();
            groupDesc = IGroup.description();
        } else {
            groupName = ISub.name().toLowerCase();
            groupDesc = ISub.description();
        }

        String name = ICommand.name();
        String description = ICommand.description();
        Environment environment = ICommand.environment();
        Permission[] permissions = ICommand.permissions();
        Type type = ICommand.type();

        List<OptionData> options = Utils.getOptions(method);

        CommandDataImpl commandData;


        if(type == Type.SLASH) {
            commandData = new CommandDataImpl(name.toLowerCase(), description);
            commandData.addOptions(options);
        } else {
            commandData = new CommandDataImpl(type, name);
        }

        LOGGER.info(String.format("Registering Command '%s' in the Group '%s'!", name, groupName));
        return commandData;
    }

    private SubcommandData getSubcommandFromMethod(Method method) {
        ICommand ICommand = method.getAnnotation(ICommand.class);

        String groupName;
        String groupDesc;

        IGroup IGroup = method.getDeclaringClass().getAnnotation(IGroup.class);
        ISubGroup ISub = method.getDeclaringClass().getAnnotation(ISubGroup.class);

        if(IGroup != null) {
            groupName = IGroup.name().toLowerCase();
        } else {
            groupName = ISub.name().toLowerCase();
        }

        String name = ICommand.name().toLowerCase();
        String description = ICommand.description();
        Type type = ICommand.type();

        List<OptionData> options = Utils.getOptions(method);

        if(type != Type.SLASH) throw new RuntimeException(String.format("Subcommand '%s' is not a Slash command!", name));

        SubcommandData subcommandData = new SubcommandData(name, description);
        subcommandData.addOptions(options);

        LOGGER.info(String.format("Registering SubCommand '%s' in the Group '%s'!", name, groupName));
        return subcommandData;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LOGGER.debug("Received Slash ICommand: " + event.getCommandPath());
        try {
            if (commandMapper.containsKey(event.getCommandPath())) {
                this.commandMapper.get(event.getCommandPath()).execute(event);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        LOGGER.debug("Received IButton: " + event.getComponentId());

        // KEY: MODAL_ID:ANYTHING...
        String id = event.getComponentId().split(":")[0];

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

        // KEY: MODAL_ID:ANYTHING....
        String key = event.getComponentId().split(":")[0];

        if(menuMapper.containsKey(key)) {
            try {
                menuMapper.get(key).invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        LOGGER.debug("Received IModal: " + event.getModalId());

        // KEY: MODAL_ID:ANYTHING....
        String key = event.getModalId().split(":")[0];

        if(modalMapper.containsKey(key)) {
            try {
                LOGGER.debug("Invoking IModal: " + key);
                modalMapper.get(key).invoke(null, event);
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
        String name = event.getName();
        LOGGER.debug("Received Message Context: " + name);

        try {
            if (commandMapper.containsKey(name)) {
                this.commandMapper.get(name).execute(event);
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

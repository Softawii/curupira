package com.softawii.curupira.core;

import com.softawii.curupira.annotations.*;
import com.softawii.curupira.properties.Environment;
import com.softawii.curupira.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Curupira extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Curupira.class);

    private final JDA JDA;
    private final Map<String, CommandHandler> commandMapper;
    private final Map<String, Method> buttonMapper;
    private final Map<String, Method> menuMapper;
    private final Map<String, Method> modalMapper;
    private final Map<String, Modal>  modals;
    private final Map<String, List<Choice>> autoCompleteMapper;
    private final boolean reset;
    private final ExceptionHandler exceptionHandler;

    public Curupira(@NotNull JDA JDA, boolean reset, ExceptionHandler exceptionHandler, String ... packages) {
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

        this.exceptionHandler = exceptionHandler;
        LOGGER.info("Curupira initialized in bot " + JDA.getSelfUser().getName());
        LOGGER.info("Commands: " + String.join(", ", commandMapper.keySet()));
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

        for(Class<?> group : groups) {
            // We have 2 levels of hierarchy
            /*
                1. Outer class (can have commands (methods) or subcommand groups (inner classes))
                2. Inner class (can have subcommands (methods))
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

                /inner execute
                 /\     /\
                  |     |
                  |     method (when the outer class is hidden)
                  |
                  inner class

                /execute
                 /\
                 |
                 method (when outer class is hidden)
             */

            IGroup igroup = group.getAnnotation(IGroup.class);
            boolean hidden = igroup.hidden();

            // Modal, Button, Menu section
            registerModalsButtonsAndMenus(group);

            // Command Section
            // Hidden -> Creating Individual Command
            if(hidden) {
                LOGGER.info(String.format("Registering Free Commands in %s", igroup.name()));
                List<Method> methods = Arrays.stream(group.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(ICommand.class)).toList();

                // Creating Individual Commands
                mapMethods(methods, null, "");

                // If it's Hidden, and we have an Inner Class, that is a group
                List<Class<?>> innerClasses = Stream.of(group.getDeclaredClasses()).filter(cls -> cls.isAnnotationPresent(ISubGroup.class)).toList();
                for(Class<?> innerClass : innerClasses) {
                    // We have 2 levels of hierarchy
                    // Example: 'inner execute'

                    ISubGroup isubgroup = (ISubGroup) innerClass.getAnnotation(ISubGroup.class);

                    registerModalsButtonsAndMenus(innerClass);

                    String inner_name = isubgroup.name().toLowerCase();
                    String inner_desc = isubgroup.description();

                    LOGGER.info(String.format("Registering CommandGroup (Sub)", inner_name));

                    CommandDataImpl commandData = new CommandDataImpl(inner_name, inner_desc);

                    // Mapping the subgroup
                    // Example: 'inner execute'
                    methods = Arrays.stream(innerClass.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(ICommand.class)).toList();
                    mapMethods(methods, commandData, inner_name);

                    // All subcommands are added, now we can send to Discord
                    if(this.reset) JDA.upsertCommand(commandData).queue();
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
                // Example 'outer execute'
                mapMethods(methods, commandData, outer_name);

                // Subcommands are added, and about SubCommandGroup????
                List<Class<?>> innerClasses = Arrays.stream(group.getClasses()).filter(cls -> cls.isAnnotationPresent(ISubGroup.class)).toList();
                for(Class innerClass : innerClasses) {
                    ISubGroup isubgroup = (ISubGroup) innerClass.getAnnotation(ISubGroup.class);

                    registerModalsButtonsAndMenus(innerClass);

                    String inner_name = isubgroup.name().toLowerCase();
                    String inner_desc = isubgroup.description();

                    LOGGER.info(String.format("Registering Commands in (Group) %s (SubGroup) %s", igroup.name(), inner_name));

                    SubcommandGroupData subcommandGroupData = new SubcommandGroupData(inner_name, inner_desc);

                    methods = Arrays.stream(innerClass.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(ICommand.class)).toList();
                    // Creating SubCommands in a SubCommandGroup
                    // Example /outer inner execute
                    String key = String.format("%s %s", outer_name, inner_name);
                    mapMethods(methods, subcommandGroupData, key);
                    commandData.addSubcommandGroups(subcommandGroupData);
                }

                // All subcommands are added, now we can send to Discord
                if(this.reset) JDA.upsertCommand(commandData).queue();
            }
        }
    }

    private void mapMethods(List<Method> methods, SerializableData parentCommand, String outerKey) {

        for(Method method : methods) {
            // TODO: Check Method Parameters
            // Generic Verification of Method Parameters

            String commandName;
            if(parentCommand != null) {
                // Can Throw an Exception, if the method is a SubCommand and the type is not SLASH
                SubcommandData subcommandData = (SubcommandData) getCommandFromMethod(method, true);
                commandName = subcommandData.getName();
                if (parentCommand instanceof SubcommandGroupData subcommandGroupData) {
                    subcommandGroupData.addSubcommands(subcommandData);
                } else if (parentCommand instanceof CommandDataImpl commandData) {
                    commandData.addSubcommands(subcommandData);
                } else {
                    throw new IllegalArgumentException("SerializableData is not a SubcommandGroupData or CommandDataImpl");
                }
            }
            // It's an individual command
            else {
                CommandDataImpl commandData = (CommandDataImpl) getCommandFromMethod(method, false);
                commandName = commandData.getName();

                // If the command is not a subcommand, we need to map it to Discord Now
                if(this.reset) JDA.upsertCommand(commandData).queue();
            }

            ICommand icommand = method.getAnnotation(ICommand.class);
            String key;
            if(outerKey.isBlank()) key = commandName;
            else                   key = String.format("%s %s", outerKey, commandName);
            addToCommandMapper(method, key, icommand);
        }
    }

    private void addToCommandMapper(Method method, String key, ICommand icommand) {

        String commandName = icommand.name().isBlank() ? method.getName().toLowerCase() : icommand.name().toLowerCase();

        CommandHandler handler = new CommandHandler(method, icommand.permissions(),
                icommand.environment(), commandName,
                icommand.description(), null);

        if(this.commandMapper.containsKey(key)) throw new RuntimeException(String.format("Duplicate Command: %s", key));
        this.commandMapper.put(key, handler);
    }

    private SerializableData getCommandFromMethod(Method method, boolean isSubcommand) {
        ICommand ICommand = method.getAnnotation(ICommand.class);
        // Throw RuntimeException
        checkParameters(method, ICommand.type());

        String groupName;

        IGroup IGroup = method.getDeclaringClass().getAnnotation(IGroup.class);
        ISubGroup ISub = method.getDeclaringClass().getAnnotation(ISubGroup.class);

        if (IGroup != null) {
            groupName = IGroup.name().toLowerCase();
        } else {
            groupName = ISub.name().toLowerCase();
        }

        String name = ICommand.name();
        String description = ICommand.description();
        Type type = ICommand.type();

        List<OptionData> options = Utils.getOptions(method);

        if (!isSubcommand) {
            CommandDataImpl commandData;
            if (type == Type.SLASH) {
                commandData = new CommandDataImpl(name.toLowerCase(), description);
                commandData.addOptions(options);
            } else {
                commandData = new CommandDataImpl(type, name);
            }
            LOGGER.info(String.format("Registering Command '%s' in the Group '%s'!", name, groupName));
            return commandData;
        } else {
            if (type != Type.SLASH)
                throw new RuntimeException(String.format("Subcommand '%s' is not a Slash command!", name));

            SubcommandData subcommandData = new SubcommandData(name, description);
            subcommandData.addOptions(options);

            LOGGER.info(String.format("Registering SubCommand '%s' in the Group '%s'!", name, groupName));
            return subcommandData;
        }
    }

    private void checkParameters(Method method, Type type) {
        Class<?>[] parameters = method.getParameterTypes();
        if(parameters.length != 1)
            throw new IllegalArgumentException(String.format("Method '%s' must have 1 parameter", method.getName()));
        if(type == Type.SLASH && !(parameters[0] == SlashCommandInteractionEvent.class))
            throw new IllegalArgumentException(String.format("Method '%s' must have a parameter of type 'SlashCommandInteractionEvent'", method.getName()));
        if(type == Type.USER && !(parameters[0] == UserContextInteractionEvent.class))
            throw new IllegalArgumentException(String.format("Method '%s' must have a parameter of type 'UserContextInteractionEvent'", method.getName()));
        if(type == Type.MESSAGE && !(parameters[0] == MessageContextInteractionEvent.class))
            throw new IllegalArgumentException(String.format("Method '%s' must have a parameter of type 'MessageContextInteractionEvent'", method.getName()));
    }

    private void registerModalsButtonsAndMenus(Class<?> clazz) {
        Utils.getMethodsAnnotatedBy(clazz, IButton.class, buttonMapper);
        Utils.getMethodsAnnotatedBy(clazz, IMenu.class  , menuMapper);
        Utils.getMethodsAnnotatedBy(clazz, IModal.class , modalMapper, (modal, method) -> {

            // No Title ? Dont need to do nothing
            if(modal.title().isEmpty()) return;

            Modal.Builder builder = Modal.create(modal.id(), modal.title());

            for(IModal.ITextInput textInput : modal.textInputs()) {
                TextInput.Builder input_builder = TextInput.create(textInput.id(), textInput.label(), textInput.style())
                        .setPlaceholder(textInput.placeholder())
                        .setRequired(textInput.required());

                if(textInput.maxLength() > 0) {
                    input_builder.setMaxLength(textInput.maxLength());
                }

                if(textInput.minLength() > 0) {
                    input_builder.setMinLength(textInput.minLength());
                }

                builder.addActionRow(input_builder.build());
            }
            Modal local_modal = builder.build();

            if(modals.containsKey(modal.id())) throw new RuntimeException("Modal with id " + modal.id() + " already exists in modals map");
            modals.put(modal.id(), local_modal);

            // Wants to create a command???
            if(modal.generate() == Type.UNKNOWN) return;

            CommandDataImpl commandData;
            if(modal.generate() == Type.SLASH) commandData = new CommandDataImpl(modal.id(), modal.description());
            else                               commandData = new CommandDataImpl(modal.generate(), modal.id());

            String id = modal.id();

            if(commandMapper.containsKey(id)) {
                LOGGER.error("ICommand with name: " + id + " already exists");
                throw new RuntimeException("ICommand with name: " + id + " already exists");
            }

            Permission[] permissions = modal.permissions();
            Environment  environment = modal.environment();
            String       name        = id;
            String       description = modal.description();

            commandMapper.put(id, new CommandHandler(method, permissions, environment,
                                                     name, description, local_modal));

            if (this.reset) this.JDA.upsertCommand(commandData).queue();

            LOGGER.debug("Added IModal as a Command: " + id);
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LOGGER.debug("Received Slash ICommand: " + event.getFullCommandName());
        try {
            if (commandMapper.containsKey(event.getFullCommandName())) {
                this.commandMapper.get(event.getFullCommandName()).execute(event);
            }
        } catch (Throwable e) {
            LOGGER.warn(e.getMessage(), e);
            if (exceptionHandler != null) {
                exceptionHandler.handle(e, event);
            }
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
            } catch (Throwable e) {
                LOGGER.warn(e.getMessage(), e);
                if (exceptionHandler != null) {
                    exceptionHandler.handle(e, event);
                }
            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        LOGGER.debug("Received Select IMenu: " + event.getComponentId());

        // KEY: MODAL_ID:ANYTHING....
        String key = event.getComponentId().split(":")[0];

        if(menuMapper.containsKey(key)) {
            try {
                menuMapper.get(key).invoke(null, event);
            } catch (Throwable e) {
                LOGGER.warn(e.getMessage(), e);
                if (exceptionHandler != null) {
                    exceptionHandler.handle(e, event);
                }
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
            } catch (Throwable e) {
                LOGGER.warn(e.getMessage(), e);
                if (exceptionHandler != null) {
                    exceptionHandler.handle(e, event);
                }
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
        } catch (Throwable e) {
            LOGGER.warn(e.getMessage(), e);
            if (exceptionHandler != null) {
                exceptionHandler.handle(e, event);
            }
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
        } catch (Throwable e) {
            LOGGER.warn(e.getMessage(), e);
            if (exceptionHandler != null) {
                exceptionHandler.handle(e, event);
            }
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

    Map<String, CommandHandler> getCommands() {
        return Collections.unmodifiableMap(commandMapper);
    }

    Map<String, Modal> getModals() {
        return Collections.unmodifiableMap(modals);
    }
}

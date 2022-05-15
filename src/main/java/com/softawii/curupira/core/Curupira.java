package com.softawii.curupira.core;

import com.softawii.curupira.annotations.*;
//import com.softawii.curupira.annotations.
import com.softawii.curupira.properties.Environment;
import com.softawii.curupira.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.interactions.command.ContextInteractionImpl;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Curupira extends ListenerAdapter {

    private JDA JDA;
    private Map<String, CommandHandler> commandMapper;
    private Map<String, Method> buttonMapper;
    private Map<String, Method> menuMapper;
    private Map<String, Method> modalMapper;
    private Map<String, List<Choice>> autoCompleteMapper;
    private MessageEmbed helpEmbed;

    public Curupira(@NotNull JDA JDA, String @NotNull ... packages) {
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
    }

    private Set<Class> getClassesInPackage(String pkgName) {
        Reflections reflections = new Reflections(pkgName, new SubTypesScanner(false));
        return new HashSet<>(reflections.getSubTypesOf(Object.class));
    }

    private void setPackage(String pkgName) {
        Set<Class> classes = getClassesInPackage(pkgName);

        // For each class in the package that is a group
        classes.stream().filter(cls -> cls.isAnnotationPresent(Group.class)).forEach(cls -> {

            System.out.println("Found Group: " + cls.getSimpleName());

            addCommands(cls);
            getMethodsAnnotatedBy(cls, Button.class, buttonMapper);
            getMethodsAnnotatedBy(cls, Menu.class  , menuMapper);
            getMethodsAnnotatedBy(cls, Modal.class , modalMapper);
        });
    }

    private void addCommands(Class cls) {
        Group group             = (Group) cls.getAnnotation(Group.class);
        List<CommandData> commands = Arrays.stream(cls.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Command.class))
                .map(getMethodCommandDataFunction(group)).collect(Collectors.toList());

        commands.forEach(command -> {
            JDA.upsertCommand(command).queue();
        });
    }

    private <T extends Annotation> void getMethodsAnnotatedBy(Class cls, Class<T> annotationClass, Map<String, Method> mapper) {
        Arrays.stream(cls.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(annotationClass)).forEach(method -> {
                T annotation = method.getAnnotation(annotationClass);
                String id = getID(annotation, method.getName());
                if(mapper.containsKey(id)) {
                    throw new RuntimeException(annotationClass.getSimpleName() + " with id " + id + " already exists");
                }

                    mapper.put(id, method);
                System.out.println("Found " + annotationClass.getSimpleName() + ": " + id);
            });
    }

    private <T extends Annotation> String getID(T annotation, String defaultID) {
        if(annotation instanceof Button) {
            String id = ((Button) annotation).id();
            return id.isBlank() ? defaultID : id;
        }
        else if(annotation instanceof Menu) {
            String id = ((Menu) annotation).id();
            return id.isBlank() ? defaultID : id;
        }
        else if(annotation instanceof Modal) {
            String id = ((Modal) annotation).id();
            return id.isBlank() ? defaultID : id;
        } else {
            throw new RuntimeException("Annotation not supported");
        }
    }

    @NotNull
    private Function<Method, CommandDataImpl> getMethodCommandDataFunction(Group group) {
        return method -> {
            System.out.println("Found Command: " + method.getName());

            Command command = method.getAnnotation(Command.class);

            String name              = (command.name().isBlank() ? method.getName() : command.name()).toLowerCase();
            String description       = command.description();
            Environment environment  = command.environment();
            Permission[] permissions = command.permissions();
            Type type                = command.type();

            List<OptionData> options = new ArrayList<>();

            // One Argument or Multiple Arguments
            if (method.isAnnotationPresent(Argument.class)) {
                Argument argument = method.getAnnotation(Argument.class);
                options.add(parserArgument(argument, name));

            } else if (method.isAnnotationPresent(Arguments.class)) {
                Argument[] arguments = method.getAnnotation(Arguments.class).value();

                for (Argument argument : arguments) {
                    OptionData optionData = parserArgument(argument, name);
                    options.add(optionData);
                }
            }

            // Range
            if (method.isAnnotationPresent(Range.class)) {
                Range range = method.getAnnotation(Range.class);
                options.addAll(parserRange(range, name));

            }
            else if(method.isAnnotationPresent(Ranges.class)) {
                Range[] ranges = method.getAnnotation(Ranges.class).value();

                for(Range range : ranges) {
                    options.addAll(parserRange(range, name));
                }
            }

            CommandDataImpl commandData;
            if(type == Type.SLASH) commandData = new CommandDataImpl(name, description);
            else                   commandData = new CommandDataImpl(type, name);
            commandData.addOptions(options);

            if(commandMapper.containsKey(name)) {
                throw new RuntimeException("Command with name: " + name + " already exists");
            }
            commandMapper.put(name, new CommandHandler(method, permissions, environment, group, name, description));

            return commandData;
        };
    }

    private OptionData parserArgument(Argument argument, String methodName) {
        String     name            = argument.name();
        String     description     = argument.description();
        boolean    required        = argument.required();
        OptionType type            = argument.type();
        boolean    hasAutoComplete = argument.hasAutoComplete();

        OptionData optionData = new OptionData(type, name, description, required, hasAutoComplete);

        if(!hasAutoComplete) {
            optionData.addChoices(Utils.getChoices(argument.choices(), type));
        } else {
            String key = methodName + ":" +  name;
            if(autoCompleteMapper.containsKey(key)) throw new RuntimeException("AutoComplete with name: " + key + " already exists");
            autoCompleteMapper.put(key, Utils.getChoices(argument.choices(), argument.type()));
        }
        return optionData;
    }

    private List<OptionData> parserRange(Range range, String methodName) {

        Argument argument = range.value();
        int min = range.min();
        int max = range.max();
        int step = range.steps();

        ArrayList<OptionData> options = new ArrayList<>();

        String description = argument.description();
        boolean required = argument.required();
        OptionType type = argument.type();
        boolean hasAutoComplete = argument.hasAutoComplete();
        List<Choice> choices = Utils.getChoices(argument.choices(), type);

        if(step <= 0) throw new RuntimeException("Steps must be greater than 0");

        for(int value = min; value <= max; value += step) {

            String name = argument.name() + value;

            OptionData optionData = new OptionData(type, name, description, required, hasAutoComplete);

            if (!hasAutoComplete) {
                optionData.addChoices(choices);
            } else {
                String key = methodName + ":" +  name;
                if(autoCompleteMapper.containsKey(key)) throw new RuntimeException("AutoComplete with name: " + name + " already exists");

                autoCompleteMapper.put(key, choices);
            }

            options.add(optionData);
        }

        return options;
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

        builder.setTitle("Command List");
        builder.setDescription("Here is a list of all commands");

        stringBuilder.forEach((groupName, localBuilder) -> {
            builder.addField(groupName, localBuilder.toString(), false);
        });

        this.JDA.upsertCommand("help", "help").queue();

        return builder.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        System.out.println("Received Slash Command: " + event.getName());
        try {
            if(event.getName().equals("help") && !commandMapper.containsKey("help")) {
                event.replyEmbeds(helpEmbed).queue();
                return;
            }
            else if (commandMapper.containsKey(event.getName())) {
                this.commandMapper.get(event.getName()).execute(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event)
    {
        String id = event.getComponentId();
        System.out.println("Received Button: " + id);

        if(buttonMapper.containsKey(id)) {
            try {
                this.buttonMapper.get(id).invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        System.out.println("Received Select Menu: " + event.getComponentId());

        if(menuMapper.containsKey(event.getComponentId())) {
            try {
                menuMapper.get(event.getComponentId()).invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        System.out.println("Received Modal: " + event.getModalId());

        if(modalMapper.containsKey(event.getModalId())) {
            try {
                System.out.println("Invoking Modal: " + event.getModalId());
                modalMapper.get(event.getModalId()).invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        System.out.println("Received User Context: " + event.getName());

        try {
            if (commandMapper.containsKey(event.getName())) {
                this.commandMapper.get(event.getName()).execute(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        System.out.println("Received Message Context: " + event.getName());

        try {
            if (commandMapper.containsKey(event.getName())) {
                this.commandMapper.get(event.getName()).execute(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String key = event.getName() + ":" + event.getFocusedOption().getName();

        System.out.println("Received Command Auto Complete: " + key);

        if(autoCompleteMapper.containsKey(key)) {
            List<Choice> choices = autoCompleteMapper.get(key)
                    .stream()
                    .filter(c -> c.getName().startsWith(event.getFocusedOption().getValue()))
                    .collect(Collectors.toList());
            event.replyChoices(choices).queue();
        }
    }
}

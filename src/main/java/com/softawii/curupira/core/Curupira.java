package com.softawii.curupira.core;

import com.softawii.curupira.annotations.*;
//import com.softawii.curupira.annotations.
import com.softawii.curupira.properties.Environment;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

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

    private MessageEmbed helpEmbed;


    public Curupira(JDA JDA, String pkg) {
        this(JDA, new String[]{pkg});
    }

    public Curupira(@NotNull JDA JDA, String @NotNull [] packages) {
        // Init
        commandMapper = new HashMap<>();
        buttonMapper  = new HashMap<>();
        menuMapper    = new HashMap<>();

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

    private boolean isGroup(Class cls) {
        return cls.isAnnotationPresent(Group.class);
    }

    private void setPackage(String pkgName) {
        Set<Class> classes = getClassesInPackage(pkgName);

        // For each class in the package that is a group
        classes.stream().filter(this::isGroup).forEach(cls -> {

            System.out.println("Found Group: " + cls.getSimpleName());

            addCommands(cls);
            addButtons(cls);
            addMenus(cls);
        });
    }

    private void addCommands(Class cls) {
        Group group             = (Group) cls.getAnnotation(Group.class);
        List<CommandData> commands = Arrays.stream(cls.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Command.class))
                .map(getMethodCommandDataFunction(group)).collect(Collectors.toList());

        JDA.updateCommands().addCommands(commands).queue();
    }

    private void addButtons(Class cls) {
        Arrays.stream(cls.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Button.class)).forEach(method -> {
                Button button = method.getAnnotation(Button.class);

                String id = button.id().isBlank() ? method.getName() : button.id();

                if(buttonMapper.containsKey(id)) {
                    throw new RuntimeException("Button with id " + id + " already exists");
                }

                buttonMapper.put(id, method);
                System.out.println("Found Button: " + id);
            });
    }

    private void addMenus(Class cls) {
        Arrays.stream(cls.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Menu.class)).forEach(method -> {
                Menu menu = method.getAnnotation(Menu.class);

                String id = menu.id().isBlank() ? method.getName() : menu.id();

                if(menuMapper.containsKey(id)) {
                    throw new RuntimeException("Menu with id " + id + " already exists");
                }

                menuMapper.put(id, method);
                System.out.println("Found Menu: " + id);
            });
    }

    @NotNull
    private Function<Method, CommandDataImpl> getMethodCommandDataFunction(Group group) {
        return method -> {
            System.out.println("Found Command: " + method.getName());

            List<OptionData> options = new ArrayList<>();

            // One Argument or Multiple Arguments
            if (method.isAnnotationPresent(Argument.class)) {
                Argument argument = method.getAnnotation(Argument.class);
                options.add(parserArgument(argument));

            } else if (method.isAnnotationPresent(Arguments.class)) {
                Argument[] arguments = method.getAnnotation(Arguments.class).value();

                for (Argument argument : arguments) {
                    options.add(parserArgument(argument));
                }
            }

            Command command = method.getAnnotation(Command.class);

            String name = (command.name().isBlank() ? method.getName() : command.name()).toLowerCase();
            String description = command.description();
            Environment environment = command.environment();
            Permission[] permissions = command.permissions();

            CommandDataImpl commandData = new CommandDataImpl(name, description);
            commandData.addOptions(options);

            if(commandMapper.containsKey(name)) {
                throw new RuntimeException("Command with name: " + name + " already exists");
            }
            commandMapper.put(name, new CommandHandler(method, permissions, environment, group, name, description));

            return commandData;
        };
    }

    private OptionData parserArgument(Argument argument) {
        String     name         = argument.name();
        String     description  = argument.description();
        boolean    required     = argument.required();
        OptionType type         = argument.type();

        return new OptionData(type, name, description, required);
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
}

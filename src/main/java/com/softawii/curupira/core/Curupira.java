package com.softawii.curupira.core;

import com.softawii.curupira.annotations.Argument;
import com.softawii.curupira.annotations.Arguments;
import com.softawii.curupira.annotations.Command;
import com.softawii.curupira.annotations.Group;
import com.softawii.curupira.properties.Environment;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.hibernate.annotations.common.reflection.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import javax.security.auth.login.LoginException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Curupira extends ListenerAdapter {

    private JDA JDA;
    private Map<String, CommandHandler> commandMapper;

    public Curupira(String pkgName, EventListener[] listeners) throws LoginException, InterruptedException {

        String token = "";

        // Init
        commandMapper = new HashMap<>();

        // Default Builder
        // We Will Build with Listeners and Slash Commands
        JDABuilder builder = JDABuilder.createDefault(token).addEventListeners(this);

        //  Adding the list of listeners to the EventListeners list
        if(listeners != null) {
            for (EventListener listener : listeners) {
                builder.addEventListeners(listener);
            }
        }
        JDA = builder.build();


        addCommands(pkgName);

        JDA.awaitReady();
    }

    private Set<Class> getClassesInPackage(String pkgName) {
        Reflections reflections = new Reflections(pkgName, new SubTypesScanner(false));
        return new HashSet<>(reflections.getSubTypesOf(Object.class));
    }

    private boolean isGroup(Class cls) {
        return cls.isAnnotationPresent(Group.class);
    }

    private void addCommands(String pkgName) {
        Set<Class> classes = getClassesInPackage(pkgName);

        // For each class in the package that is a group
        classes.stream().filter(this::isGroup).forEach(cls -> {
            Group group             = (Group) cls.getAnnotation(Group.class);

            System.out.println("Found Group: " + cls.getSimpleName());

            List<CommandData> commands = Arrays.stream(cls.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Command.class))
                    .map(getMethodCommandDataFunction(group)).collect(Collectors.toList());

            JDA.updateCommands().addCommands(commands).queue();
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

            String name = command.name();
            String description = command.description();
            Environment environment = command.environment();
            Permission[] permissions = command.permissions();

            CommandDataImpl commandData = new CommandDataImpl(name, description);
            commandData.addOptions(options);

            if(commandMapper.containsKey(name)) {
                throw new RuntimeException("Command with name: " + name + " already exists");
            }
            commandMapper.put(name, new CommandHandler(method, permissions, environment, group));

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

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        System.out.println("Received Slash Command: " + event.getName());
        try {
            this.commandMapper.get(event.getName()).execute(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

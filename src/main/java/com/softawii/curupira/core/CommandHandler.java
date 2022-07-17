package com.softawii.curupira.core;

import com.softawii.curupira.annotations.IGroup;
import com.softawii.curupira.exceptions.InvalidChannelTypeException;
import com.softawii.curupira.exceptions.MissingPermissionsException;
import com.softawii.curupira.properties.Environment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * A package-private class that is used to handle commands.
 *
 * <p>
 *     This class is responsible for handling commands. It is responsible for checking permissions, environment and
 *     call the method or throws an error if something is wrong.
 * </p>
 */
class CommandHandler {

    private static final ChannelType[] PUBLIC_CHANNELS = {
            ChannelType.GUILD_PUBLIC_THREAD,
            ChannelType.GUILD_NEWS_THREAD,
            ChannelType.NEWS,
            ChannelType.GUILD_PRIVATE_THREAD,
            ChannelType.TEXT
    };

    private static final ChannelType[] PRIVATE_CHANNELS = {
            ChannelType.PRIVATE,
            ChannelType.GROUP
    };

    private final Method          method;
    private final Permission[]    permissions;
    private final Environment     environment;

    private final String          name;
    private final String          description;
    private final Modal           modal;

    CommandHandler(Method method, Permission[] permissions, Environment environment, String name, String description, Modal modal) {
        this.method      = method;
        this.permissions = permissions;
        this.environment = environment;
        this.name        = name;
        this.description = description;
        this.modal       = modal;
    }

    /**
     * Checks if the command can be executed in the given channel.
     *
     * @param channelType
     * @param member
     * @return true if the command can be executed in the given channel, false otherwise.
     * @throws InvalidChannelTypeException if the channel type is not supported.
     * @throws MissingPermissionsException if the user does not have the required permissions.
     */
    private boolean canExecute(ChannelType channelType, Member member) throws InvalidChannelTypeException, MissingPermissionsException {
        // Check for Private Channels
        if(environment.equals(Environment.PRIVATE)) {
            if(Arrays.stream(PRIVATE_CHANNELS).anyMatch(c -> c.equals(channelType))) {
                return true;
            }
            else {
                throw new InvalidChannelTypeException("Invalid Channel Type");
            }
        }

        // Check for Public Channels
        if(environment.equals(Environment.SERVER)) {
            // Is a Public Channel??
            if(Arrays.stream(PUBLIC_CHANNELS).anyMatch(c -> c.equals(channelType))) {
                // Permissions??
                if(member.hasPermission(permissions)) {
                    return true;
                }
                throw new MissingPermissionsException("Missing Permissions");
            }
            else {
                throw new InvalidChannelTypeException("Invalid Channel Type");
            }
        }

        // Both
        return true;
    }

    /**
     * Executes the command or just print the stacktrace. (This is used for debugging, but it's temporary)
     * @param event
     */
    public void execute(SlashCommandInteractionEvent event) {
        try {
            if (canExecute(event.getChannelType(), event.getMember())) {
                if(modal == null) method.invoke(null, event);
                else event.replyModal(modal).queue();
            }
        } catch (InvalidChannelTypeException | InvocationTargetException | IllegalAccessException | MissingPermissionsException e) {
            e.printStackTrace();
        }
    }

    public void execute(UserContextInteractionEvent event) {
        try {
            if (canExecute(event.getChannelType(), event.getMember())) {
                if(modal == null) method.invoke(null, event);
                else event.replyModal(modal).queue();
            }
        } catch (InvalidChannelTypeException | InvocationTargetException | IllegalAccessException | MissingPermissionsException e) {
            e.printStackTrace();
        }
    }

    public void execute(MessageContextInteractionEvent event) {
        try {
            if (canExecute(event.getChannelType(), event.getMember())) {
                if(modal == null) method.invoke(null, event);
                else event.replyModal(modal).queue();
            }
        } catch (InvalidChannelTypeException | InvocationTargetException | IllegalAccessException | MissingPermissionsException e) {
            e.printStackTrace();
        }
    }

    public void execute(ModalInteractionEvent event) {
        try {
            if (canExecute(event.getChannelType(), event.getMember())) {
                method.invoke(null, event);
            }
        } catch (InvalidChannelTypeException | InvocationTargetException | IllegalAccessException | MissingPermissionsException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

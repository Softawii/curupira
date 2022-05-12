package com.softawii.curupira.core;

import com.softawii.curupira.annotations.Group;
import com.softawii.curupira.exceptions.InvalidChannelTypeException;
import com.softawii.curupira.exceptions.MissingPermissionsException;
import com.softawii.curupira.properties.Environment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CommandHandler {

    private static ChannelType[] publicChannels = {
            ChannelType.GUILD_PUBLIC_THREAD,
            ChannelType.GUILD_NEWS_THREAD,
            ChannelType.NEWS,
            ChannelType.GUILD_PRIVATE_THREAD,
            ChannelType.TEXT
    };

    private static ChannelType[] privateChannels = {
            ChannelType.PRIVATE,
            ChannelType.GROUP
    };

    private Method          method;
    private Permission[]    permissions;
    private Environment     environment;
    private Group           group;

    public CommandHandler(Method method, Permission[] permissions, Environment environment, Group group) {
        this.method      = method;
        this.permissions = permissions;
        this.environment = environment;
        this.group       = group;
    }

    private boolean canExecute(ChannelType channelType, Member member) throws InvalidChannelTypeException, MissingPermissionsException {
        // Check for Private Channels
        if(environment.equals(Environment.PRIVATE)) {
            if(Arrays.stream(privateChannels).anyMatch(c -> c.equals(channelType))) {
                return true;
            }
            else {
                throw new InvalidChannelTypeException("Invalid Channel Type");
            }
        }

        // Check for Public Channels
        if(environment.equals(Environment.SERVER)) {
            // Is a Public Channel??
            if(Arrays.stream(publicChannels).anyMatch(c -> c.equals(channelType))) {
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

    public void execute(SlashCommandInteractionEvent event) {
        try {
            if (canExecute(event.getChannelType(), event.getMember())) {
                method.invoke(null, event);
            }
        } catch (InvalidChannelTypeException | InvocationTargetException | IllegalAccessException | MissingPermissionsException e) {
            e.printStackTrace();
        }
    }
}

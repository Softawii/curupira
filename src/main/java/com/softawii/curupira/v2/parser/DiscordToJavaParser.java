package com.softawii.curupira.v2.parser;

import com.softawii.curupira.v2.annotations.DiscordParameter;
import com.softawii.curupira.v2.annotations.LocaleType;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.enums.LocaleTypeEnum;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.lang.reflect.Parameter;

public class DiscordToJavaParser {

    public static Object getParameterFromEvent(CommandInteractionPayload event, Parameter parameter, LocalizationManager localization) {
        if(LocalizationManager.class.isAssignableFrom(parameter.getType())) {
            return localization;
        }
        else if(JDA.class.isAssignableFrom(parameter.getType())) {
            return event.getJDA();
        }
        else if(parameter.getType().equals(DiscordLocale.class) && parameter.isAnnotationPresent(LocaleType.class)) {
            if(parameter.getAnnotation(LocaleType.class).value() == LocaleTypeEnum.GUILD) return event.getGuildLocale();
            else return event.getUserLocale();
        }
        else if(parameter.getType().equals(SlashCommandInteractionEvent.class)) {
            return event;
        }
        else if(parameter.getType().equals(String.class)) {
            return getString(event, parameter);
        }
        else if(parameter.getType().equals(Double.class)) {
            return getDouble(event, parameter);
        }
        else if(parameter.getType().equals(Integer.class)) {
            return getInteger(event, parameter);
        }
        else if(parameter.getType().equals(Long.class)) {
            return getLong(event, parameter);
        }
        else if(parameter.getType().equals(Boolean.class)) {
            return getBoolean(event, parameter);
        }
        else if(parameter.getType().equals(User.class)) {
            return getUser(event, parameter);
        }
        else if(parameter.getType().equals(Member.class)) {
            return getMember(event, parameter);
        }
        else if(parameter.getType().equals(Guild.class)) {
            return getGuild(event, parameter);
        }
        else if(MessageChannelUnion.class.isAssignableFrom(parameter.getType())) {
            return getChannel(event, parameter);
        }
        else {
            return null;
        }
    }

    private static Object getString(CommandInteractionPayload event, Parameter parameter) {
        DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
        if(annotation != null) {
            OptionMapping option = event.getOption(annotation.name());
            if(option != null) return option.getAsString();
            else return null;
        }
        else {
            return null;
        }
    }

    private static Object getDouble(CommandInteractionPayload event, Parameter parameter) {
        DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
        if(annotation != null) {
            OptionMapping option = event.getOption(annotation.name());
            if(option != null) return option.getAsDouble();
            else return null;
        }
        else {
            return null;
        }
    }

    private static Object getInteger(CommandInteractionPayload event, Parameter parameter) {
        DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
        if(annotation != null) {
            OptionMapping option = event.getOption(annotation.name());
            if(option != null) return option.getAsInt();
            else return null;
        }
        else {
            return null;
        }
    }

    private static Object getLong(CommandInteractionPayload event, Parameter parameter) {
        DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
        if(annotation != null) {
            OptionMapping option = event.getOption(annotation.name());
            if(option != null) return option.getAsLong();
            else return null;
        }
        else {
            return null;
        }
    }

    private static Object getBoolean(CommandInteractionPayload event, Parameter parameter) {
        DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
        if(annotation != null) {
            OptionMapping option = event.getOption(annotation.name());
            if(option != null) return option.getAsBoolean();
            else return null;
        }
        else {
            return null;
        }
    }

    private static Object getUser(CommandInteractionPayload event, Parameter parameter) {
        DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
        if(annotation != null) {
            OptionMapping option = event.getOption(annotation.name());
            if(option != null) return option.getAsUser();
            else return null;
        }
        else {
            RequestInfo requestInfo = parameter.getAnnotation(RequestInfo.class);
            if(requestInfo != null) return event.getUser();
            else return null;
        }
    }

    private static Object getMember(CommandInteractionPayload event, Parameter parameter) {
        DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
        if(annotation != null) {
            OptionMapping option = event.getOption(annotation.name());
            if(option != null) return option.getAsMember();
            else return null;
        }
        else {
            RequestInfo requestInfo = parameter.getAnnotation(RequestInfo.class);
            if(requestInfo != null) return event.getMember();
            else return null;
        }
    }

    private static Object getGuild(CommandInteractionPayload event, Parameter parameter) {
        return event.getGuild();
    }

    private static Object getChannel(CommandInteractionPayload event, Parameter parameter) {
        DiscordParameter annotation = parameter.getAnnotation(DiscordParameter.class);
        if(annotation != null) {
            OptionMapping option = event.getOption(annotation.name());
            if(option != null) return option.getAsChannel();
            else return null;
        }
        else {
            RequestInfo requestInfo = parameter.getAnnotation(RequestInfo.class);
            if(requestInfo != null) return event.getChannel();
            else return null;
        }
    }

}

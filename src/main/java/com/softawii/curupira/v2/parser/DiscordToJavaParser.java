package com.softawii.curupira.v2.parser;

import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import com.softawii.curupira.v2.annotations.LocaleType;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.annotations.interactions.DiscordField;
import com.softawii.curupira.v2.enums.LocaleTypeEnum;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.lang.reflect.Parameter;

public class DiscordToJavaParser {

    // TODO: V2.? Refactor this method to use a switch statement or a map to avoid the if-else chain
    public static Object getParameterFromEvent(Interaction event, Parameter parameter, LocalizationManager localization) {
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
        else if(parameter.getType().equals(AutoCompleteQuery.class) && event instanceof CommandAutoCompleteInteractionEvent autoComplete) {
            return autoComplete.getFocusedOption();
        }
        // TODO: probably i can group all "return event" in one thing, but i don't want to do it now
        // Commands
        else if(parameter.getType().equals(SlashCommandInteractionEvent.class) && event instanceof SlashCommandInteractionEvent) {
            return event;
        }
        else if(parameter.getType().equals(UserContextInteractionEvent.class) && event instanceof UserContextInteractionEvent) {
            return event;
        }
        else if(parameter.getType().equals(MessageContextInteractionEvent.class) && event instanceof MessageContextInteractionEvent) {
            return event;
        }
        // Other Interactions
        else if(parameter.getType().equals(ModalInteractionEvent.class) && event instanceof ModalInteractionEvent) {
            return event;
        }
        else if(parameter.getType().equals(ButtonInteractionEvent.class) && event instanceof ButtonInteractionEvent) {
            return event;
        }
        else if(parameter.getType().equals(StringSelectInteractionEvent.class) && event instanceof StringSelectInteractionEvent) {
            return event;
        }
        else if(parameter.getType().equals(EntitySelectInteractionEvent.class) && event instanceof EntitySelectInteractionEvent) {
            return event;
        }
        else if(parameter.getType().isAssignableFrom(Interaction.class)) {
            return event;
        }
        else if(parameter.getType().equals(String.class) && event instanceof CommandInteractionPayload payload) {
            return getString(payload, parameter);
        }
        else if(parameter.getType().equals(String.class) && event instanceof ModalInteractionEvent payload) {
            return getString(payload, parameter);
        }
        else if(parameter.getType().equals(Double.class) && event instanceof CommandInteractionPayload payload) {
            return getDouble(payload, parameter);
        }
        else if(parameter.getType().equals(Integer.class) && event instanceof CommandInteractionPayload payload) {
            return getInteger(payload, parameter);
        }
        else if(parameter.getType().equals(Long.class) && event instanceof CommandInteractionPayload payload) {
            return getLong(payload, parameter);
        }
        else if(parameter.getType().equals(Boolean.class) && event instanceof CommandInteractionPayload payload) {
            return getBoolean(payload, parameter);
        }
        else if(parameter.getType().equals(User.class) && event instanceof CommandInteractionPayload payload) {
            return getUser(payload, parameter);
        }
        else if(parameter.getType().equals(Member.class) && event instanceof CommandInteractionPayload payload) {
            return getMember(payload, parameter);
        }
        else if(parameter.getType().equals(User.class)) {
            return event.getUser();
        }
        else if(parameter.getType().equals(Member.class)) {
            return event.getMember();
        }
        else if(parameter.getType().equals(Guild.class)) {
            return event.getGuild();
        }
        else if(MessageChannelUnion.class.isAssignableFrom(parameter.getType()) && event instanceof CommandInteractionPayload payload) {
            return getChannel(payload, parameter);
        }
        else if(GuildChannelUnion.class.isAssignableFrom(parameter.getType()) && event instanceof CommandInteractionPayload payload) {
            return getChannel(payload, parameter);
        }
        else if(parameter.getType().equals(GuildChannelUnion.class)) {
            return event.getChannel();
        }
        else if(parameter.getType().equals(MessageChannelUnion.class)) {
            return event.getChannel();
        }
        else {
            return null;
        }
    }

    private static Object getString(ModalInteractionEvent payload, Parameter parameter) {
        DiscordField annotation = parameter.getAnnotation(DiscordField.class);
        if(annotation != null) {
            ModalMapping value = payload.getValue(annotation.value());
            if(value != null) return value.getAsString();
            else return null;
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

package com.softawii.curupira.v2.parser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;

import java.util.Collection;
import java.util.List;

public class JavaToDiscordParser {

    public static OptionType getTypeFromClass(Class clazz) {
        if (clazz.equals(String.class)) {
            return OptionType.STRING;
        } else if (clazz.equals(Double.class)) {
            return OptionType.NUMBER;
        } else if (clazz.equals(Integer.class)) {
            return OptionType.INTEGER;
        } else if (clazz.equals(Long.class)) {
            return OptionType.INTEGER;
        } else if (clazz.equals(Boolean.class)) {
            return OptionType.BOOLEAN;
        } else if(clazz.equals(User.class)) {
            return OptionType.USER;
        } else if(clazz.equals(Member.class)) {
            return OptionType.USER;
        } else if(clazz.equals(Role.class)) {
            return OptionType.ROLE;
        } else if(MessageChannelUnion.class.isAssignableFrom(clazz)) {
            return OptionType.CHANNEL;
        } else if(Channel.class.isAssignableFrom(clazz)) {
            return OptionType.CHANNEL;
        } else
            throw new RuntimeException("Type not supported");
    }

    public static void responseFromCommandEvent(GenericCommandInteractionEvent event, Object result, boolean ephemeral) {
        if(result instanceof String response) {
            event.reply(response).setEphemeral(ephemeral).queue();
        } else if(result instanceof MessageCreateData message) {
            event.reply(message).setEphemeral(ephemeral).queue();
        } else if(result instanceof Modal modal) {
            event.replyModal(modal).queue();
        } else if(result instanceof MessageEmbed embed) {
            event.replyEmbeds(embed).setEphemeral(ephemeral).queue();
        } else if(result instanceof Collection<?> collection && collection.stream().findFirst().get() instanceof MessageEmbed) {
            Collection<MessageEmbed> collectionEmbed = (Collection<MessageEmbed>) collection;
            event.replyEmbeds(collectionEmbed).setEphemeral(ephemeral).queue();
        }
        else if(result instanceof Collection<?> collection && collection.stream().findFirst().get() instanceof FileUpload) {
             Collection<FileUpload> collectionFiles = (Collection<FileUpload>) collection;
             event.replyFiles(collectionFiles).setEphemeral(ephemeral).queue();
        } else if(result instanceof MessagePollData poll) {
            event.replyPoll(poll).setEphemeral(true).queue();
        } else {
            throw new RuntimeException("Type not supported");
        }
    }
}

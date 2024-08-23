package com.softawii.curupira.v2.parser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionType;

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
}

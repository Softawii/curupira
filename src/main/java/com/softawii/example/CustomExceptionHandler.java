package com.softawii.example;

import com.softawii.curupira.core.ExceptionHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.lang.reflect.InvocationTargetException;

public class CustomExceptionHandler implements ExceptionHandler {
    @Override
    public void handle(Throwable throwable, Interaction interaction) {
        JDA jda = interaction.getJDA();
        User user = interaction.getUser();
        TextChannel textChannel = interaction.getTextChannel();

        if (throwable instanceof InvocationTargetException e) {
            if (interaction instanceof IReplyCallback i) {
               i.reply(throwable.getMessage()).queue();
               return;
            }
        }
        textChannel.sendMessage("Failed to call command").queue();
    }
}
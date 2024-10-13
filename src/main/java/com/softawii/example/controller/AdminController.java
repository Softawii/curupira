package com.softawii.example.controller;

import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.interactions.DiscordButton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.List;

@DiscordController(value = "admin2", description = "hello!", guildId = 856534404779868180L, permissions = Permission.ADMINISTRATOR)
public class AdminController {

    private static final int guildPageSize = 5;
    private static final String adminGuildNextPage = "admin-guild-next-page";
    private static final String adminGuildPrevPage = "admin-guild-prev-page";

    private MessageEmbed paginateGuilds(List<Guild> guilds, int page) {
        int maxPages = (int) Math.ceil((double) guilds.size() / AdminController.guildPageSize);

        if(page < 0) {
            page = 0;
        } else if(page >= maxPages) {
            page = maxPages - 1;
        }

        // Get the guilds for the current page
        int start = page * AdminController.guildPageSize;
        int end = Math.min(start + AdminController.guildPageSize, guilds.size());
        List<Guild> guildsPage = guilds.subList(start, end);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Guilds (Page " + (page + 1) + "/" + maxPages + ")")
                .setColor(Color.CYAN);

        for (Guild guild : guildsPage) {
            embed.addField(guild.getName(), "ID: " + guild.getId(), false);
        }

        return embed.build();
    }

    private void sendGetGuildsResponse(IReplyCallback event, JDA jda, int page) {
        int maxPages = (int) Math.ceil((double) jda.getGuilds().size() / AdminController.guildPageSize);

        Button next = Button.primary(adminGuildNextPage + ':' + (page + 1), "Next").withDisabled(page == maxPages - 1);
        Button prev = Button.primary(adminGuildPrevPage + ':' + (page - 1), "Previous").withDisabled(page == 0);

        MessageEmbed embed = paginateGuilds(jda.getGuilds(), page);

        event.replyEmbeds(embed).addActionRow(prev, next).setEphemeral(true).queue();
    }

    @DiscordCommand(name = "guilds", description = "list all guilds", ephemeral = true)
    public void getGuildsCommand(SlashCommandInteractionEvent event, JDA jda) {
        sendGetGuildsResponse(event, jda, 0);
    }

    @DiscordButton(name = adminGuildPrevPage)
    public void getPreviousPage(ButtonInteractionEvent event, JDA jda) {
        int page = Integer.parseInt(event.getComponentId().split(":")[1]);
        sendGetGuildsResponse(event, jda, page);
        event.getMessage().delete().queue();
    }

    @DiscordButton(name = adminGuildNextPage)
    public void getNextPage(ButtonInteractionEvent event, JDA jda) {
        int page = Integer.parseInt(event.getComponentId().split(":")[1]);
        sendGetGuildsResponse(event, jda, page);
        event.getMessage().delete().queue();
    }
}

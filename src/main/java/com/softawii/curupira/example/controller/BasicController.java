package com.softawii.curupira.example.controller;

import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@DiscordController(value = "basic", description = "Basic Controller")
public class BasicController {

    @DiscordCommand(name = "hello", description = "Hello World", ephemeral = true)
    public String hello(@RequestInfo Member member) {
        return "Hello World, " + member.getNickname() + "!";
    }

    @DiscordCommand(name = "goodbye", description = "Goodbye World", ephemeral = true)
    public MessageEmbed goodbye(JDA jda, Guild guild, @RequestInfo Member member) {
        MessageEmbed embed = new EmbedBuilder().setTitle("Goodbye, " + guild.getName()).setDescription("Goodbye, " + member.getNickname() + "!").setFooter(jda.getSelfUser().getEffectiveName()).build();
        return embed;
    }

    @DiscordCommand(name = "response", description = "Response")
    public void response(SlashCommandInteractionEvent event) {
        MessageEmbed embed = new EmbedBuilder().setTitle("Hello World!").build();
        event.reply("Hello World!").setEphemeral(true).addEmbeds(embed).setEphemeral(true).queue();
    }

    @DiscordCommand(name = "embed", description = "Embed")
    public String greetings(@RequestInfo Member member,
                            @DiscordParameter(name = "name", description = "Your name") String name,
                            @DiscordParameter(name = "occupation", description = "Your occupation") String occupation,
                            @DiscordParameter(name = "channel", description = "Channel", required = false) GuildChannelUnion channel) {
        return "Hello, " + name + "! You are a " + occupation + "!";
    }

}

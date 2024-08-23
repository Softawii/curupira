package com.softawii.curupira.example;

import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.DiscordParameter;
import com.softawii.curupira.v2.annotations.RequestInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@DiscordController(value = "bar", description = "foo foo foo", parent = "foo")
public class Foo {

    @DiscordCommand(name = "baz", description = "baz baz baz")
    public void baz(SlashCommandInteractionEvent event,
                    @RequestInfo Member member,
                    @DiscordParameter(name = "name", description = "Your name") String name,
                    @DiscordParameter(name = "age", description = "Your age") Integer age,
                    @DiscordParameter(name = "channel", description = "Select channel") MessageChannelUnion channel) {
        channel.sendMessage("test").queue();
        event.reply("Hello " + name + ", you are " + age + " years old and your id is " + member.getId()).queue();
    }

    @DiscordCommand(name = "qux", description = "qux qux qux")
    public void qux(SlashCommandInteractionEvent event,
                    @RequestInfo Member member,
                    @DiscordParameter(name = "hello", description = "channel to send hello") MessageChannelUnion channel) {
        channel.sendMessage("hello chat").queue();
        event.reply("sent").setEphemeral(true).queue();
    }
}

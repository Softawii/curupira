package com.softawii.curupira.example;

import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.DiscordParameter;
import com.softawii.curupira.v2.annotations.RequestInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessagePollData;

@DiscordController(value = "bar", description = "foo foo foo", parent = "fuo")
public class Foo {

    @DiscordCommand(name = "baz", description = "baz baz baz", ephemeral = true)
    public String baz(SlashCommandInteractionEvent event,
                    @RequestInfo Member member,
                    @DiscordParameter(name = "name", description = "Your name") String name,
                    @DiscordParameter(name = "age", description = "Your age") Integer age,
                    @DiscordParameter(name = "channel", description = "Select channel") MessageChannelUnion channel) {
        channel.sendMessage("test").queue();

        return "Hello " + name + " you are " + age + " years old";
    }

    @DiscordCommand(name = "qux", description = "qux qux qux")
    public String qux(SlashCommandInteractionEvent event,
                    @RequestInfo Member member,
                    @DiscordParameter(name = "hello", description = "channel to send hello") MessageChannelUnion channel) {
        channel.sendMessage("hello chat").queue();
        return "Hello " + member.getEffectiveName();
    }

    @DiscordCommand(name = "charlie", description = "charlie charlie charlie")
    public MessagePollData charlie(
                    @RequestInfo Member member,
                    @DiscordParameter(name = "poll", description = "pool name") String name) {

        return MessagePollData.builder(name).addAnswer("yes").addAnswer("no").build();
    }
}

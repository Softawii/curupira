package com.softawii.curupira.example;

import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.DiscordParameter;
import com.softawii.curupira.v2.annotations.RequestInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@DiscordController(value = "foo", description = "foo foo foo", hidden = true)
public class Foo {

    @DiscordCommand(name = "bar", description = "bar bar bar")
    public void bar(SlashCommandInteractionEvent event,
                    @RequestInfo Member member,
                    @DiscordParameter(name = "name", description = "Your name") String name,
                    @DiscordParameter(name = "age", description = "Your age") Integer age,
                    @DiscordParameter(name = "channel", description = "Select channel") MessageChannelUnion channel) {
        channel.sendMessage("test").queue();
        event.reply("Hello " + name + ", you are " + age + " years old and your id is " + member.getId()).queue();
    }
}

@DiscordController(value = "foo", description = "foo foo foo")
class Foo2 {

    @DiscordCommand(name = "bar", description = "bar bar bar")
    public void bar(SlashCommandInteractionEvent event,
                    @RequestInfo Member member,
                    @DiscordParameter(name = "name", description = "Your name") String name,
                    @DiscordParameter(name = "age", description = "Your age") Integer age,
                    @DiscordParameter(name = "channel", description = "Select channel") MessageChannelUnion channel) {
        channel.sendMessage("test").queue();
        event.reply("Hello " + name + ", you are " + age + " years old and your id is " + member.getId()).queue();
    }
}

package com.softawii.curupira.example;

import com.softawii.curupira.v2.annotations.DiscordCommand;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.DiscordParameter;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.w3c.dom.Text;

@DiscordController(value = "bar", description = "foo foo foo", parent = "foo", permissions = {Permission.ADMINISTRATOR},
                    resource = "i18n", locales = {DiscordLocale.ENGLISH_US}, defaultLocale = DiscordLocale.PORTUGUESE_BRAZILIAN)
public class Foo {

//    @DiscordCommand(name = "baz", description = "baz baz baz", ephemeral = true)
//    public String baz(SlashCommandInteractionEvent event,
//                    @RequestInfo Member member,
//                    @DiscordParameter(name = "name", description = "Your name") String name,
//                    @DiscordParameter(name = "age", description = "Your age") Integer age,
//                    @DiscordParameter(name = "channel", description = "Select channel") MessageChannelUnion channel) {
//        channel.sendMessage("test").queue();
//
//        return "Hello " + name + " you are " + age + " years old";
//    }

//    @DiscordCommand(name = "qux", description = "qux qux qux")
//    public String qux(SlashCommandInteractionEvent event,
//                    @RequestInfo Member member,
//                    @DiscordParameter(name = "hello", description = "channel to send hello") MessageChannelUnion channel) {
//        channel.sendMessage("hello chat").queue();
//        return "Hello " + member.getEffectiveName();
//    }
//
    @DiscordCommand(name = "charlie", description = "charlie charlie charlie")
    public TextLocaleResponse charlie(
                    @RequestInfo Member member,
                    @DiscordParameter(name = "poll", description = "pool name") String name) {

        return new TextLocaleResponse("foo.bar.charlie.response.ok", name);
    }
}

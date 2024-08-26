package com.softawii.curupira.example.controller;

import com.softawii.curupira.v2.annotations.*;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.messages.MessagePollData;

@DiscordController(value = "bar", description = "foo foo foo", parent = "foo", permissions = {Permission.ADMINISTRATOR},
                    resource = "i18n", locales = {DiscordLocale.PORTUGUESE_BRAZILIAN})
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

    @DiscordCommand(name = "qux", description = "qux qux qux")
    public MessagePollData qux(JDA jda,
                               LocalizationManager localization,
                               @RequestInfo Member member,
                               @LocaleType DiscordLocale locale,
                               @DiscordParameter(name = "title", description = "embed title") String title,
                               @DiscordParameter(name = "description", description = "embed description") String description) {

        String titleMessage = localization.getLocalizedString("foo.bar.qux.embed.title", locale, title, member.getNickname());
        String descriptionMessage = localization.getLocalizedString("foo.bar.qux.embed.description", locale, member.getEffectiveName(), jda.getSelfUser().getEffectiveName());

        return MessagePollData.builder(titleMessage).addAnswer("yes").addAnswer("no").build();
    }

    @DiscordCommand(name = "charlie", description = "charlie charlie charlie")
    public TextLocaleResponse charlie(
                    @RequestInfo Member member,
                    @DiscordParameter(name = "name", description = "your name", choices = {
                            @DiscordChoice(name = "foo"),
                            @DiscordChoice(name = "bar"),
                            @DiscordChoice(name = "baz")
                    }) String name,
                    @DiscordParameter(name= "profession", description = "your profession", autoComplete = true) String profession) {

        return new TextLocaleResponse("foo.bar.charlie.response.ok", name);
    }

    @DiscordAutoComplete(name = "charlie")
    public Command.Choice[] charlieAutoComplete(AutoCompleteQuery query) {
        if(query.getName().equals("profession")) {
            return new Command.Choice[] {
                    new Command.Choice("developer", "developer"),
                    new Command.Choice("designer", "designer"),
                    new Command.Choice("tester", "tester")
            };
        }
        return new Command.Choice[0];
    }
}

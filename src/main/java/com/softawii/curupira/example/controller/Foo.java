package com.softawii.curupira.example.controller;

import com.softawii.curupira.v2.annotations.*;
import com.softawii.curupira.v2.annotations.commands.DiscordAutoComplete;
import com.softawii.curupira.v2.annotations.commands.DiscordChoice;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import com.softawii.curupira.v2.annotations.interactions.DiscordField;
import com.softawii.curupira.v2.annotations.interactions.DiscordMenu;
import com.softawii.curupira.v2.annotations.interactions.DiscordModal;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessagePollData;

@DiscordController(value = "bar", description = "foo foo foo", parent = "foo", permissions = {Permission.ADMINISTRATOR},
                    resource = "i18n", locales = {DiscordLocale.PORTUGUESE_BRAZILIAN})
public class Foo {

    @DiscordCommand(name = "baz", description = "baz baz baz", ephemeral = true)
    public LayoutComponent baz(SlashCommandInteractionEvent event,
                                   @RequestInfo Member member,
                                   @DiscordParameter(name = "name", description = "Your name") String name,
                                   @DiscordParameter(name = "age", description = "Your age") Integer age) {

        StringSelectMenu build = StringSelectMenu.create("select-menu")
                .addOption("foo", "foo")
                .addOption("bar", "bar")
                .addOption("baz", "baz")
                .build();
        return ActionRow.of(build);
    }

    @DiscordMenu(name = "select-menu")
    public void selectMenu(StringSelectInteractionEvent event) {
        String selections = event.getSelectedOptions().stream().map(option -> option.getLabel()).reduce((a, b) -> a + ", " + b).orElse("none");
        event.reply("You selected: " + selections).setEphemeral(true).queue();
    }

    @DiscordCommand(name = "qux", description = "qux qux qux", ephemeral = true)
    public Modal qux(JDA jda,
                               LocalizationManager localization,
                               @RequestInfo Member member,
                               @LocaleType DiscordLocale locale,
                               @DiscordParameter(name = "title", description = "embed title") String title,
                               @DiscordParameter(name = "description", description = "embed description") String description) {

        String titleMessage = localization.getLocalizedString("foo.bar.qux.embed.title", locale, title, member.getNickname());
        String descriptionMessage = localization.getLocalizedString("foo.bar.qux.embed.description", locale, member.getEffectiveName(), jda.getSelfUser().getEffectiveName());

        return Modal.create("modal-test", "Modal Test")
                .addComponents(
                        ActionRow.of(TextInput.create("text-input", "Text Input", TextInputStyle.SHORT).build()),
                        ActionRow.of(TextInput.create("text-input-2", "Text Input 2", TextInputStyle.PARAGRAPH).build())
                ).build();
    }

    @DiscordModal(name = "modal-test", ephemeral = true)
    public String modalTest(ModalInteractionEvent event,
                            @DiscordField("text-input") String textInput,
                            @DiscordField("text-input-2") String textInput2) {
        return "You typed: " + textInput + " and " + textInput2;
    }

    @DiscordCommand(name = "charlie", description = "charlie charlie charlie")
    public TextLocaleResponse charlie(
                    @RequestInfo Member member,
                    @DiscordParameter(name = "name", description = "your name", choices = {
                            @DiscordChoice(name = "foo"),
                            @DiscordChoice(name = "bar"),
                            @DiscordChoice(name = "baz")
                    }) String name,
                    @DiscordParameter(name = "occupation", description = "your occupation", autoComplete = true) String occupation,
                    @DiscordParameter(name = "food", description = "your favorite food", autoComplete = true) String food) {

        return new TextLocaleResponse("foo.bar.charlie.response.ok", name);
    }

    @DiscordAutoComplete(name = "charlie")
    public Command.Choice[] charlieAutoComplete(AutoCompleteQuery query) {
        if(query.getName().equals("occupation")) {
            return new Command.Choice[] {
                    new Command.Choice("developer", "developer"),
                    new Command.Choice("designer", "designer"),
                    new Command.Choice("tester", "tester")
            };
        }
        return new Command.Choice[0];
    }

    @DiscordAutoComplete(name = "charlie", variable = "food")
    public Command.Choice[] charlieAutoCompleteFood(AutoCompleteQuery query) {
        return new Command.Choice[]{
                new Command.Choice("pizza", "pizza"),
                new Command.Choice("hamburger", "hamburger"),
                new Command.Choice("hotdog", "hotdog")
        };
    }
}

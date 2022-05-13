package com.softawii.example;

import com.softawii.curupira.annotations.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import static net.dv8tion.jda.api.interactions.components.buttons.Button.*;

@Group(name="Group 1", description="Group Description")
public class Example {

    private static final String strMenu = "menu:class";

    @Command(description="Command Description", permissions={})
    @Argument(name="name", description="Name", required=true, type= OptionType.STRING)
    @Argument(name="age", description="Age", required=true, type=OptionType.INTEGER)
    public static void Introduce(SlashCommandInteractionEvent event) {
        String msg = "Hello " + event.getOption("name").getAsString() + "! are you " + event.getOption("age").getAsInt() + " years old?";

        event.reply(msg)
            .addActionRow(
                primary("Confirm", "Confirm"),
                danger("Cancel", "Cancel")
            )
            .queue();
    }

    @Command(description="Command Description", permissions={})
    public static void Menu(SlashCommandInteractionEvent event) {
        SelectMenu menu = SelectMenu.create(strMenu)
                .setRequiredRange(1, 25)
                .setPlaceholder("Choose your class") // shows the placeholder indicating what this menu is for
                .addOption("Arcane Mage", "mage-arcane")
                .addOption("Fire Mage", "mage-fire")
                .addOption("Frost Mage", "mage-frost")
                .build();

        event.reply("Please pick your class below")
                //.setEphemeral(true)
                .addActionRow(menu)
                .queue();
    }

    @Command(description="Command Description", permissions={})
    public static void Support(SlashCommandInteractionEvent event) {
        TextInput email = TextInput.create("email", "Email", TextInputStyle.SHORT)
                .setPlaceholder("Enter your E-mail")
                .setMinLength(10)
                .setMaxLength(100) // or setRequiredRange(10, 100)
                .build();

        TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Your concerns go here")
                .setMinLength(30)
                .setMaxLength(1000)
                .build();

        net.dv8tion.jda.api.interactions.components.Modal modal = net.dv8tion.jda.api.interactions.components.Modal.create("support", "Support")
                .addActionRows(ActionRow.of(email), ActionRow.of(body))
                .build();

        event.replyModal(modal).queue();
    }

    @Modal(id="support")
    public static void Support(@NotNull ModalInteractionEvent event) {
        String email = event.getValue("email").getAsString();
        String body = event.getValue("body").getAsString();

        event.reply("Thanks for your request!").setEphemeral(true).queue();
    }

    @Button(id="Confirm")
    public static void Confirm(ButtonInteractionEvent event) {

        event.editMessage("Are you sure??")
            .setActionRow(
                primary("Yes", "Yes"),
                danger("Cancel", "No"))
            .queue();
    }

    @Button(id="Cancel")
    public static void Cancel(ButtonInteractionEvent event) {
        event.reply("Canceled").queue();
    }

    @Menu(id=strMenu)
    public static void Menu(@NotNull SelectMenuInteractionEvent event) {
        System.out.println("I'm here");
        event.getInteraction().getSelectedOptions().stream().map(option -> option.getLabel()).reduce((a, b) -> a + " " + b).ifPresent(s -> event.reply(s).queue());
    }
}

package com.softawii.example;

import com.softawii.curupira.annotations.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static net.dv8tion.jda.api.interactions.components.buttons.Button.*;

@IGroup(name="IGroup 1", description="IGroup Description")
public class Example {

    private static final String strMenu = "menu:class";

    @ICommand(description="ICommand Description", permissions={})
    @IArgument(name="name", description="Name", required=true, type= OptionType.STRING, choices = {@IArgument.Choice(key="John"), @IArgument.Choice(key="Jane")})
    @IArgument(name="age", description="Age", required=true, type=OptionType.INTEGER,
            hasAutoComplete = true,
            choices={@IArgument.Choice(key="Um", value="1"), @IArgument.Choice(key="Um", value="1"), @IArgument.Choice(key="Dois", value="2"), @IArgument.Choice(key="Tres", value="3"), @IArgument.Choice(key="Quatro", value="4")})
    public static void Introduce(SlashCommandInteractionEvent event) {
        String msg = "Hello " + event.getOption("name").getAsString() + "! are you " + event.getOption("age").getAsInt() + " years old?";

        event.reply(msg)
            .addActionRow(
                primary("Confirm", "Confirm"),
                danger("Cancel", "Cancel")
            )
            .queue();
    }

    @ICommand(description="ICommand Description")
    @IRange(value=@IArgument(name="name", description="name of users", required=false, type=OptionType.STRING,
            hasAutoComplete = true, choices = {@IArgument.Choice(key="Yan"), @IArgument.Choice(key="Eduardo"), @IArgument.Choice(key="Romulo"), @IArgument.Choice(key="Nicolas")})
            , min=0, max=15)
    public static void Names(SlashCommandInteractionEvent event) {

        ArrayList<String> names = new ArrayList<>();

        for(int i = 0; i < 16; i++) {
            if(event.getOption("name" + i) != null)
                names.add(event.getOption("name" + i).getAsString());
        }

        event.reply("Names: " + String.join(", ", names)).queue();
    }

    @ICommand(description="ICommand Description", permissions={})
    public static void Menu(MessageContextInteractionEvent event) {
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

    @ICommand(description="ICommand Description", permissions={})
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

    @ICommand(description="ICommand Description", permissions={})
    public static void Foo(SlashCommandInteractionEvent event) {
        System.out.println("hi");

        TextInput name = TextInput.create("name", "Name", TextInputStyle.SHORT)
                .setPlaceholder("Enter your Name")
                .setMinLength(10)
                .setMaxLength(100) // or setRequiredRange(10, 100)
                .build();

        net.dv8tion.jda.api.interactions.components.Modal modal = net.dv8tion.jda.api.interactions.components.Modal.create("support", "Support")
                .addActionRows(ActionRow.of(name))
                .build();

        event.replyModal(modal).queue();
    }

    @IModal(id="support")
    public static void Support(@NotNull ModalInteractionEvent event) {
        String email = event.getValue("email").getAsString();
        String body = event.getValue("body").getAsString();

        event.reply("Thanks for your request!").setEphemeral(true).queue();
    }

    @IButton(id="Confirm")
    public static void Confirm(ButtonInteractionEvent event) {

        event.editMessage("Are you sure??")
            .setActionRow(
                primary("Yes", "Yes"),
                danger("Cancel", "No"))
            .queue();
    }

    @IButton(id="Cancel")
    public static void Cancel(ButtonInteractionEvent event) {
        event.reply("Canceled").queue();
    }

    @IMenu(id=strMenu)
    public static void Menu(@NotNull SelectMenuInteractionEvent event) {
        System.out.println("I'm here");
        event.getInteraction().getSelectedOptions().stream().map(option -> option.getLabel()).reduce((a, b) -> a + " " + b).ifPresent(s -> event.reply(s).queue());
    }
}

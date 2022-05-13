package com.softawii.example;

import com.softawii.curupira.annotations.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
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

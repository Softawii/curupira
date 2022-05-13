package com.softawii.example;

import com.softawii.curupira.annotations.Argument;
import com.softawii.curupira.annotations.ButtonAnnotation;
import com.softawii.curupira.annotations.Command;
import com.softawii.curupira.annotations.Group;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Group(name="Group 1", description="Group Description")
public class Example {

    @Command(description="Command Description", permissions={})
    @Argument(name="name", description="Name", required=true, type= OptionType.STRING)
    @Argument(name="age", description="Age", required=true, type=OptionType.INTEGER)
    public static void Introduce(SlashCommandInteractionEvent event) {
        String msg = "Hello " + event.getOption("name").getAsString() + "! are you " + event.getOption("age").getAsInt() + " years old?";

        event.reply(msg)
            .addActionRow(
                Button.primary("Confirm", "Confirm"),
                Button.danger("Cancel", "Cancel")
            )
            .queue();
    }

    @ButtonAnnotation(id="Confirm")
    public static void Confirm(ButtonInteractionEvent event) {

        event.editMessage("Are you sure??")
            .setActionRow(
                Button.primary("Yes", "Yes"),
                Button.danger("Cancel", "No"))
            .queue();
    }

    @ButtonAnnotation(id="Cancel")
    public static void Cancel(ButtonInteractionEvent event) {
        event.reply("Canceled").queue();
    }
}

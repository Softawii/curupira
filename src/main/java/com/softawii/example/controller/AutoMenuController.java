package com.softawii.example.controller;

import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.commands.DiscordAutoComplete;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import com.softawii.curupira.v2.annotations.interactions.DiscordMenu;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;

@DiscordController(value = "auto-menu", description = "Auto Menu Controller")
public class AutoMenuController {

    @DiscordCommand(name = "menu", description = "menu command")
    public LayoutComponent menu(@DiscordParameter(name = "name", description = "Your name", autoComplete = true) String name,
                                @DiscordParameter(name = "occupation", description = "Your occupation", autoComplete = true) String occupation) {

        return ActionRow.of(
                StringSelectMenu.create("auto-menu-select")
                        .addOption("foo", "foo")
                        .addOption("bar", "bar")
                        .addOption("baz", "baz")
                        .build()
        );
    }

    @DiscordAutoComplete(name = "menu", variable = "name")
    public Command.Choice[] menuAutoCompleteName(AutoCompleteQuery query) {
        List<Command.Choice> choices = List.of(
                new Command.Choice("John Doe", "John Doe"),
                new Command.Choice("Jane Doe", "Jane Doe"),
                new Command.Choice("John Smith", "John Smith")
        );

        return choices.stream().filter(choice -> choice.getName().toLowerCase().contains(query.getValue().toLowerCase())).toArray(Command.Choice[]::new);
    }

    @DiscordAutoComplete(name = "menu", variable = "occupation")
    public Command.Choice[] menuAutoCompleteOccupation(AutoCompleteQuery query) {
        List<Command.Choice> choices = List.of(
                new Command.Choice("Developer", "Developer"),
                new Command.Choice("Designer", "Designer"),
                new Command.Choice("Tester", "Tester")
        );

        return choices.stream().filter(choice -> choice.getName().toLowerCase().contains(query.getValue().toLowerCase())).toArray(Command.Choice[]::new);
    }

    @DiscordMenu(name = "auto-menu-select")
    public String selectMenu(StringSelectInteractionEvent event) {
        return "You selected: " + event.getSelectedOptions().get(0).getLabel();
    }

}

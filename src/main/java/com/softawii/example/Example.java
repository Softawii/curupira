package com.softawii.example;

import com.softawii.curupira.annotations.Argument;
import com.softawii.curupira.annotations.Command;
import com.softawii.curupira.annotations.Group;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Group(name="Group 1", description="Group Description")
public class Example {

    @Command(description="Command Description", permissions={})
    @Argument(name="name", description="Name", required=false)
    @Argument(name="age", description="Age", required=false)
    public static void Command(SlashCommandInteractionEvent event) {
        event.getChannel().sendMessage("Hi").queue();
    }
}

package com.softawii.example;

import com.softawii.curupira.annotations.Argument;
import com.softawii.curupira.annotations.Command;
import com.softawii.curupira.annotations.Group;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Group(name="Example", description="Hi")
public class Example {

    @Command(name="hello", description="Hello", permissions={})
    @Argument(name="name", description="Name", required=false)
    @Argument(name="age", description="Age", required=false)
    public static void Command(SlashCommandInteractionEvent event) {
        event.getChannel().sendMessage("Hi").queue();
    }
}

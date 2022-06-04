package com.softawii.example;

import com.softawii.Main;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import com.softawii.curupira.annotations.ISubGroup;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.Modal;

@IGroup(name = "Foo", description = "Foo Group", hidden = false)
public class Foo {

    @ISubGroup(name = "Bar", description = "Bar Group")
    public class Bar {

        @ICommand(name = "bar", description = "bar bar bar", type=Command.Type.SLASH)
        public static void bar(SlashCommandInteractionEvent event) {
            System.out.println("bar");
            event.reply("bar bar bar").queue();
        }

        @ICommand(name = "baz", description = "baz baz baz", type=Command.Type.SLASH)
        public static void baz(SlashCommandInteractionEvent event) {
            System.out.println("baz");
            event.reply("baz baz baz").queue();
        }
    }

    @ICommand(name = "foo", description = "foo foo foo", type=Command.Type.SLASH)
    public static void foo(SlashCommandInteractionEvent event) {
        System.out.println("foo foo foo");
        event.reply("foo foo foo").queue();
    }

    @ICommand(name = "fuu", description = "fuu fuu fuu", type=Command.Type.SLASH)
    public static void fuu(SlashCommandInteractionEvent event) {
        System.out.println("fuu fuu fuu");
        event.reply("fuu fuu fuu").queue();
    }
}

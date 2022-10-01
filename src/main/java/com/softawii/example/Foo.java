package com.softawii.example;

import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import com.softawii.curupira.annotations.IModal;
import com.softawii.curupira.annotations.ISubGroup;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

@IGroup(name = "Foo", description = "Foo Group", hidden = true)
public class Foo {

    @ISubGroup(name = "Bar", description = "Bar Group")
    public static class Bar {

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

    @ICommand(name = "faa", description = "faa faa faa", type=Command.Type.MESSAGE)
    public static void faa(MessageContextInteractionEvent event) {
        System.out.println("faa faa faa");
        event.reply("faa faa faa").queue();
    }

    @IModal(id="support", title="Support", description="Support Description", generate=Command.Type.SLASH,
            textInputs = {@IModal.ITextInput(id="name", label="Name", style= TextInputStyle.SHORT, placeholder="Enter your Name", minLength=3, maxLength=100, required=true)})
    public static void Support(@NotNull ModalInteractionEvent event) {
        String name = event.getValue("name").getAsString();
        event.reply("Thanks for your request " + name).setEphemeral(true).queue();
    }
}

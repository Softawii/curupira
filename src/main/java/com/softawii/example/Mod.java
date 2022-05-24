package com.softawii.example;

import com.softawii.Main;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import com.softawii.curupira.annotations.IModal;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.Objects;

@IGroup(name = "Mod", description = "Mod group")
public class Mod {

    @ICommand(name = "report", description = "Report an user", type=Command.Type.MESSAGE)
    public static void Report(MessageContextInteractionEvent event) {

        System.out.println("Im here");
        
        Modal.Builder builder = Main.curupira.getModal("report");

        String name = event.getTarget().getAuthor().getName();
        builder.setTitle(builder.getTitle() + name);
        String new_id = builder.getId() + ":" + event.getTarget().getAuthor().getId() + ":" + event.getTarget().getContentStripped();
        builder.setId(new_id);
        event.replyModal(builder.build()).queue();
    }

    @IModal(id="report", description = "Report a user", title="Report ",
    textInputs = {@IModal.ITextInput(id="motivation", placeholder="They are very disrespectful!", style= TextInputStyle.PARAGRAPH, required=true, label = "What is the reason for the report?")})
    public static void Report(ModalInteractionEvent event) {
        System.out.println("Im here");
        String motivation    = event.getValue("motivation") != null ? event.getValue("motivation").getAsString() : "";
        String[] raw         = event.getModalId().split(":");
        String targetUser    = raw[1];
        String targetContent = raw[2];

        event.getGuild().retrieveMemberById(targetUser).queue(member -> {
            event.reply("Thank you for reporting " + event.getUser().getAsMention() +
                            ", Reported: " + member.getEffectiveName() +
                            ", because of " + targetContent + " for the reason: " + motivation)
                    .setEphemeral(true)
                    .queue();
        });
    }
}

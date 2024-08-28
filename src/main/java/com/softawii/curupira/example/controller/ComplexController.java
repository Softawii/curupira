package com.softawii.curupira.example.controller;

import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.annotations.commands.DiscordChoice;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import com.softawii.curupira.v2.annotations.interactions.DiscordButton;
import com.softawii.curupira.v2.annotations.interactions.DiscordField;
import com.softawii.curupira.v2.annotations.interactions.DiscordModal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;

@DiscordController(parent = "very", value = "complex", description = "Complex Controller", permissions = { Permission.ADMINISTRATOR})
public class ComplexController {

    @DiscordCommand(name = "form", description = "Form", ephemeral = true)
    public Modal formRegister(@RequestInfo Member member,
                              Guild guild,
                              @DiscordParameter(name = "type", description = "Type of form", choices = {
                                      @DiscordChoice(name = "Validation", value = "validation"),
                                      @DiscordChoice(name = "Report", value = "report")
                              }) String type) {
        if(type.equals("validation")) {
            return Modal.create("complex-modal-validation", guild.getName() + " - Validation Form")
                    .addActionRow(TextInput.create("validation-name", "Name", TextInputStyle.SHORT).setPlaceholder("John Doe").build())
                    .addActionRow(TextInput.create("validation-email", "Email", TextInputStyle.SHORT).setPlaceholder("johndoe@mail.com").build())
                    .build();
        } else {
            return Modal.create("complex-modal-report", guild.getName() + " - Report Form")
                    .addActionRow(TextInput.create("report-id", "UserId", TextInputStyle.SHORT).setPlaceholder("12802383984391").build())
                    .addActionRow(TextInput.create("report-motivation", "Motivation", TextInputStyle.SHORT).setPlaceholder("He's very silly").build())
                    .build();
        }
    }

    @DiscordModal(name = "complex-modal-validation", ephemeral = true)
    public void validationForm(@RequestInfo Member member, Guild guild,
                               @DiscordField("validation-name") String validationName,
                               @DiscordField("validation-email") String validationEmail) {
        member.getUser().openPrivateChannel().queue(channel -> {
            channel.sendMessage("Validation Form: \nName: " + validationName + "\nEmail: " + validationEmail).queue();
        });
    }

    @DiscordModal(name = "complex-modal-report", ephemeral = true)
    public void reportForm(@RequestInfo Member member, Guild guild, JDA jda,
                           ModalInteractionEvent event,
                           @DiscordField("report-id") String reportId,
                           @DiscordField("report-motivation") String reportMotivation) {

        Member reported = guild.getMemberById(reportId);

        if(reported == null) {
            event.reply("User not found").setEphemeral(true).queue();
            return;
        }

        MessageEmbed embed = new EmbedBuilder().setTitle("Report").setColor(Color.RED)
                .setDescription("User: " + reported.getAsMention() + "\nMotivation: " + reportMotivation)
                .setFooter("Reported by: " + member.getEffectiveName(), member.getUser().getAvatarUrl())
                .build();

        Button confirm = Button.primary("apply-report-action:" + reportId, "Confirm");
        Button cancel = Button.danger("cancel-report-action", "Cancel");

        event.replyEmbeds(embed).addActionRow(confirm, cancel).setEphemeral(true).queue();
    }

    @DiscordButton(name = "apply-report-action", ephemeral = true)
    public String applyReportAction(ButtonInteractionEvent event, Guild guild) {
        String id = event.getComponentId().split(":")[1];
        Member reported = guild.getMemberById(id);

        if(reported == null) {
            return "User not found";
        }

        event.getMessage().delete().queue();
        return "Report applied to " + reported.getAsMention();
    }

    @DiscordButton(name = "cancel-report-action", ephemeral = true)
    public String cancelReportAction(ButtonInteractionEvent event) {
        event.getMessage().delete().queue();
        return "Report canceled";
    }
}

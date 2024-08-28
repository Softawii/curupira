package com.softawii.curupira.example.controller;

import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.LocaleType;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@DiscordController(value = "translated", description = "Translated Controller", resource = "translated", locales = DiscordLocale.PORTUGUESE_BRAZILIAN)
public class TranslatedController {

    @DiscordCommand(name = "hello", description = "Hello command", ephemeral = true)
    public TextLocaleResponse hello(@RequestInfo Member member,
                                    @DiscordParameter(name = "name", description = "Your name") String name) {

        return new TextLocaleResponse("translated.hello.response", name);
    }

    @DiscordCommand(name = "bye", description = "Bye command", ephemeral = true)
    public String bye(@RequestInfo Member member,
                      @LocaleType DiscordLocale userLocale,
                      LocalizationManager localization,
                      @DiscordParameter(name = "name", description = "Your name") String name) {

        return localization.getLocalizedString("translated.bye.response", userLocale, name);
    }
}

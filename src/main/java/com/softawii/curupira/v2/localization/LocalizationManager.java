package com.softawii.curupira.v2.localization;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import org.slf4j.helpers.MessageFormatter;

import java.text.MessageFormat;
import java.util.Map;

public class LocalizationManager {
    private final LocalizationFunction localization;
    private final DiscordLocale defaultLocale;

    public LocalizationManager(LocalizationFunction localization, DiscordLocale defaultLocale) {
        this.localization = localization;
        this.defaultLocale = defaultLocale;
    }

    public String getLocalizedString(String key, DiscordLocale userLocale, Object ... args) {
        Map<DiscordLocale, String> locales = localization.apply(key);
        String mask;
        if(locales.containsKey(userLocale)) {
            mask = locales.get(userLocale);
        } else {
            mask = locales.get(defaultLocale);
        }

        return MessageFormat.format(mask, args);
    }
}

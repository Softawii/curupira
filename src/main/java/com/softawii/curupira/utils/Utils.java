package com.softawii.curupira.utils;

import com.softawii.curupira.annotations.Choice;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<Command.Choice> getChoices(Choice[] args, OptionType optionType) {
        // Long, Double, String
        ArrayList<Command.Choice> choices = new ArrayList<>();
        for(Choice arg : args) {
            String key = arg.key();
            String value = arg.value().isBlank() ? key : arg.value();

            if(optionType == OptionType.STRING) {
                choices.add(new Command.Choice(key, value));
            } else if(optionType == OptionType.INTEGER) {
                choices.add(new Command.Choice(key, Integer.parseInt(value)));
            } else if(optionType == OptionType.NUMBER) {
                choices.add(new Command.Choice(key, Double.parseDouble(value)));
            } else {
                throw new RuntimeException("OptionType not supported");
            }
        }

        return choices;
    }
}

package com.softawii.curupira.utils;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<Command.Choice> getChoices(String[] args, OptionType optionType) {
        // Long, Double, String
        ArrayList<Command.Choice> choices = new ArrayList<>();
        for(String arg : args) {
            if(optionType == OptionType.STRING) {
                choices.add(new Command.Choice(arg, arg));
            } else if(optionType == OptionType.INTEGER) {
                choices.add(new Command.Choice(arg, Integer.parseInt(arg)));
            } else if(optionType == OptionType.NUMBER) {
                choices.add(new Command.Choice(arg, Double.parseDouble(arg)));
            } else {
                throw new RuntimeException("OptionType not supported");
            }
        }

        return choices;
    }
}

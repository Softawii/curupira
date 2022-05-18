package com.softawii.curupira.utils;

import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;

public interface ParserCallback {
    void operation(String key, List<Command.Choice> choices);
}

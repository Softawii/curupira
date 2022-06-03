package com.softawii.curupira.core;

import net.dv8tion.jda.api.interactions.Interaction;

public interface ExceptionHandler {
    void handle(Throwable throwable, Interaction interaction);
}
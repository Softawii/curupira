package com.softawii;

import com.softawii.curupira.core.Curupira;
import com.softawii.curupira.core.ExceptionHandler;
import com.softawii.example.CustomExceptionHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        String token = System.getenv("discord_token");
        String pkg   = "com.softawii.example";

        // Default Builder
        // We Will Build with Listeners and Slash Commands
        JDABuilder builder = JDABuilder.createDefault(token);
        JDA JDA = builder.build();
        boolean reset = true;
        ExceptionHandler exceptionHandler = new CustomExceptionHandler();
        Curupira curupira = new Curupira(JDA, reset, exceptionHandler, pkg);

        JDA.awaitReady();
    }
}
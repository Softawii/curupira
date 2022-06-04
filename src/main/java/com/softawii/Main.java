package com.softawii;

import com.softawii.curupira.core.Curupira;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import javax.security.auth.login.LoginException;
import java.util.EventListener;

public class Main {

    public static Curupira curupira;
    public static void main(String[] args) throws LoginException, InterruptedException {

        String token = "OTM5OTcyODU1MDg3NjUyOTM0.GFYXVw.Zxjk9If6kI6sV5f4UdbsOsQUKD8d2kXoScrT4Q";
        String pkg   = "com.softawii.example";

        // Default Builder
        // We Will Build with Listeners and Slash Commands
        JDABuilder builder = JDABuilder.createDefault(token);
        JDA JDA = builder.build();
        boolean reset = true;

        Main.curupira = new Curupira(JDA, reset, pkg);

        JDA.awaitReady();
    }
}
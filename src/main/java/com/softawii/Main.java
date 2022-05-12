package com.softawii;

import com.softawii.curupira.core.Curupira;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.util.EventListener;

public class Main {
    public static void main(String[] args) throws LoginException, InterruptedException {

        String token = "No token";
        String pkg   = "com.softawii.example";

        // Default Builder
        // We Will Build with Listeners and Slash Commands
        JDABuilder builder = JDABuilder.createDefault(token);
        JDA JDA = builder.build();

        Curupira curupira = new Curupira(JDA, pkg);

        JDA.awaitReady();
    }
}
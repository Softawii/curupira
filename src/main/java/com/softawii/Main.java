package com.softawii;

import com.softawii.example.controller.*;
import com.softawii.example.exceptions.GenericExceptionHandler;
import com.softawii.curupira.v2.core.CurupiraBoot;
import com.softawii.curupira.v2.integration.BasicContextProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, NoSuchMethodException {
        BasicContextProvider context = new BasicContextProvider();

        String token = System.getenv("discord_token");
        String pkg   = "com.softawii.example";

        context.registerInstance(GenericExceptionHandler.class, new GenericExceptionHandler());
        context.registerInstance(BasicController.class, new BasicController());
        context.registerInstance(ComplexController.class, new ComplexController());
        context.registerInstance(TranslatedController.class, new TranslatedController());
        context.registerInstance(AutoMenuController.class, new AutoMenuController());
        context.registerInstance(AdminController.class, new AdminController());

        JDABuilder builder = JDABuilder.createDefault(token)
                .enableCache(CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL);
        JDA JDA = builder.build();
        JDA.awaitReady();

        boolean reset = true;
        CurupiraBoot curupira = new CurupiraBoot(JDA, context, reset, pkg);

        JDA.awaitReady();
    }
}
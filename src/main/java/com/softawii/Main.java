package com.softawii;

import com.softawii.curupira.example.controller.Foo;
import com.softawii.curupira.example.exceptions.ExceptionHandler;
import com.softawii.curupira.v2.core.CurupiraBoot;
import com.softawii.curupira.v2.integration.BasicContextProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, NoSuchMethodException {
        BasicContextProvider context = new BasicContextProvider();

        String token = System.getenv("discord_token");
        String pkg   = "com.softawii.curupira.example";

        context.registerInstance(Foo.class, new Foo());
        context.registerInstance(ExceptionHandler.class, new ExceptionHandler());

        JDABuilder builder = JDABuilder.createDefault(token);
        JDA JDA = builder.build();

        boolean reset = false;
        CurupiraBoot curupira = new CurupiraBoot(JDA, context, reset, pkg);

        JDA.awaitReady();
    }


    public static void test(Long a, Long b, String c, Double d) {
        logger.info("a={}, b={}, c={}, d={}", a, b, c, d);
    }
}
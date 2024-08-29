package com.softawii.curupira.v2.core;


import com.softawii.curupira.v2.annotations.DiscordExceptions;
import com.softawii.curupira.v2.core.handler.ExceptionHandler;
import com.softawii.curupira.v2.integration.ContextProvider;
import com.softawii.curupira.v2.localization.LocalizationManager;
import com.softawii.curupira.v2.utils.ScanUtils;
import net.dv8tion.jda.api.interactions.Interaction;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ExceptionMapper {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionMapper.class);
    private final ContextProvider context;
    private final Map<String, ExceptionHandler> handlerByPackage;
    private final Map<Class<?>, ExceptionHandler> handlerByClass;
    private ExceptionHandler defaultHandler;

    public ExceptionMapper(ContextProvider context, String ... packages) {
        this.context = context;
        this.handlerByPackage = new HashMap<>();
        this.handlerByClass = new HashMap<>();
        this.defaultHandler = null;

        scanPackages(packages);
    }

    private Set<Class> getClassesInPackage(String pkg, Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(pkg, new SubTypesScanner(false));
        return new HashSet<>(reflections.getSubTypesOf(Object.class)).stream().filter(clazz -> clazz.getPackage().getName().startsWith(pkg) && clazz.isAnnotationPresent(annotation)).collect(Collectors.toSet());
    }

    private void scanPackages(String ... packages) {
        // get all classes with DiscordExceptions annotation
        Set<Class> classes = new HashSet<>();

        for(String pkg : packages) {
            classes.addAll(getClassesInPackage(pkg, DiscordExceptions.class));
        }

        for(Class<?> clazz : classes) {
            scanClass(clazz);
        }
    }

    private void scanClass(Class<?> clazz) {
        DiscordExceptions annotation = clazz.getAnnotation(DiscordExceptions.class);
        ExceptionHandler handler = new ExceptionHandler(context.getInstance(clazz));

        // define providers
        if(annotation.classes().length == 0 && annotation.packages().length == 0) {
            defaultHandler = handler;
        } else {
            for(Class<?> exception : annotation.classes()) {
                handlerByClass.put(exception, handler);
            }

            for(String pkg : annotation.packages()) {
                handlerByPackage.put(pkg, handler);
            }
        }
    }

    private ExceptionHandler findHandlerByCaller(Class<?> caller) {
        // 1. option 1 - check if the caller class is in the map
        ExceptionHandler handler = handlerByClass.get(caller);

        // 2. option 2 - super classes
        if(handler == null) {
            for (Class<?> superClass = caller.getSuperclass(); superClass != null; superClass = superClass.getSuperclass()) {
                handler = handlerByClass.get(superClass);
                if (handler != null) {
                    break;
                }
            }
        }

        // 3. option 3 - check if the caller package is in the map
        if (handler == null) {
            String pkg = caller.getPackageName();
            while (!pkg.isEmpty()) {
                handler = handlerByPackage.get(pkg);
                if (handler != null) {
                    break;
                }

                // Move up to the superpackage
                int lastDotIndex = pkg.lastIndexOf('.');
                if (lastDotIndex == -1) {
                    break;
                }
                pkg = pkg.substring(0, lastDotIndex);
            }
        }

        if (handler == null) {
            handler = defaultHandler;
        }

        return handler;
    }

    public void handle(Class<?> caller, Throwable exception, Interaction interaction, LocalizationManager localization) {
        ExceptionHandler handler = findHandlerByCaller(caller);

        if(handler != null) {
            handler.handle(exception, interaction, localization);
        } else {
            throw new RuntimeException("No handler found for exception: " + exception.getClass().getName());
        }
    }

}

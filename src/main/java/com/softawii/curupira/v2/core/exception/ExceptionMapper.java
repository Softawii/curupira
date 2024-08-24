package com.softawii.curupira.v2.core.exception;


import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.DiscordException;
import com.softawii.curupira.v2.annotations.DiscordExceptions;
import com.softawii.curupira.v2.integration.ContextProvider;
import com.softawii.curupira.v2.utils.ScanUtils;
import net.dv8tion.jda.api.interactions.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExceptionMapper {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionMapper.class);
    private final Map<Class<? extends Throwable>, Method> handlers;
    private final ContextProvider context;
    private Object instance;

    public ExceptionMapper(ContextProvider context, String ... packages) {
        this.handlers = new HashMap<>();
        this.context = context;

        scanPackages(packages);
    }

    private void scanPackages(String ... packages) {
        // get all classes with DiscordExceptions annotation
        Set<Class> classes = new HashSet<>();

        for(String pkg : packages) {
            classes.addAll(ScanUtils.getClassesInPackage(pkg).stream().filter(clazz -> clazz.isAnnotationPresent(DiscordExceptions.class)).toList());
        }

        if(classes.stream().count() > 1) {
            throw new RuntimeException("Only one class can have the DiscordExceptions annotation");
        }

        if(classes.stream().count() == 1) {
            scanClass(classes.stream().findFirst().get());
        }
    }

    private void scanClass(Class<?> clazz) {
        this.instance = context.getInstance(clazz);

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DiscordException.class)) {
                DiscordException annotation = method.getAnnotation(DiscordException.class);

                if(method.getParameters().length != 2 && method.getParameters()[0].getType() != Throwable.class && method.getParameters()[1].getType() != Interaction.class) {
                    throw new RuntimeException("Invalid handler method signature: " + method.getName());
                }

                for (Class<? extends Throwable> exception : annotation.value()) {
                    handlers.put(exception, method);
                }
            }
        }
    }

    private Method findHandlerMethod(Class<?> exceptionClass) {
        Method handlerMethod = handlers.get(exceptionClass);
        if (handlerMethod == null) {
            // Check for superclass handlers if a direct one is not found
            for (Class<?> superClass = exceptionClass.getSuperclass(); superClass != null; superClass = superClass.getSuperclass()) {
                handlerMethod = handlers.get(superClass);
                if (handlerMethod != null) {
                    break;
                }
            }
        }
        return handlerMethod;
    }

    public void handle(Throwable exception, Interaction interaction) {
        Class<?> exceptionClass = exception.getClass();
        Method handlerMethod = findHandlerMethod(exceptionClass);

        if (handlerMethod != null) {
            try {
                // Invoke the handler method
                handlerMethod.invoke(this.instance, exception, interaction);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke handler method: " + handlerMethod.getName(), e);
            }
        } else {
            logger.error("Unhandled exception", exception);
        }
    }

}

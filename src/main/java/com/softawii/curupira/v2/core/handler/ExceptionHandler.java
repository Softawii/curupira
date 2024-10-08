package com.softawii.curupira.v2.core.handler;

import com.softawii.curupira.v2.annotations.DiscordException;
import com.softawii.curupira.v2.localization.LocalizationManager;
import com.softawii.curupira.v2.parser.DiscordToJavaParser;
import net.dv8tion.jda.api.interactions.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    private final Object instance;
    private final Map<Class<? extends Throwable>, Method> handlers;

    public ExceptionHandler(Object instance) {
        this.instance = instance;
        this.handlers = new HashMap<>();
        scanMethods();
        checkMinimumRequirements();
    }

    private void scanMethods() {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(DiscordException.class)) {
                DiscordException annotation = method.getAnnotation(DiscordException.class);

                if(Arrays.stream(method.getParameters()).noneMatch(parameter -> parameter.getType() == Throwable.class)) {
                    throw new RuntimeException("Invalid handler method signature: " + method.getName());
                }

                if(Arrays.stream(method.getParameters()).noneMatch(parameter -> parameter.getType() == Interaction.class)) {
                    throw new RuntimeException("Invalid handler method signature: " + method.getName());
                }

                for (Class<? extends Throwable> exception : annotation.value()) {
                    handlers.put(exception, method);
                }
            }
        }
    }

    private void checkMinimumRequirements() {
        // only allow a class with a generic exception handler
        if(handlers.isEmpty()) {
            throw new RuntimeException("No exception handlers found");
        }

        // only allow a class with a generic exception handler
        if(!handlers.containsKey(Throwable.class)) {
            throw new RuntimeException("You need to provide a Throwable handler in " + this.instance.getClass().getName());
        }
    }

    private Method getHandler(Class<?> exception) {
        Method handler = handlers.get(exception);

        if(handler == null) {
            for (Class<?> superClass = exception.getSuperclass(); superClass != null; superClass = superClass.getSuperclass()) {
                handler = handlers.get(superClass);
                if (handler != null) {
                    break;
                }
            }
        }

        return handler;
    }

    private Object[] getParameters(Method method, Interaction event, LocalizationManager localization, Throwable exception) {
        List<Object> parameters = new ArrayList<>();

        for(Parameter parameter : method.getParameters()) {
            if (parameter.getType().isNestmateOf(Throwable.class)) {
                parameters.add(exception);
            } else {
                parameters.add(DiscordToJavaParser.getParameterFromEvent(event, parameter, localization));
            }
        }
        return parameters.toArray();
    }

    public void handle(Throwable exception, Interaction interaction, LocalizationManager localization) {
        if(exception instanceof InvocationTargetException invocation) {
            exception = invocation.getTargetException();
        }

        Method handler = getHandler(exception.getClass());

        if(handler != null) {
            try {
                handler.invoke(instance, getParameters(handler, interaction, localization, exception));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke handler method: " + handler.getName(), e);
            }
        } else {
            LOGGER.error("Unhandled exception", exception);
        }
    }

}

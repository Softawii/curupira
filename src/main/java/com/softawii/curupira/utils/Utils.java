package com.softawii.curupira.utils;

import com.softawii.curupira.annotations.*;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class Utils {

    private static final Logger LOGGER = LogManager.getLogger(Utils.class);

    /**
     * This function is used to extract the choices from the annotation.
     *
     * @param args Array of IChoices annotations
     * @param optionType the option type from the command
     * @return a map of choices
     * @throws RuntimeException if the option type is not supported or the cast fails
     */
    public static List<Command.Choice> getChoices(IArgument.IChoice[] args, OptionType optionType) {
        // Long, Double, String
        ArrayList<Command.Choice> choices = new ArrayList<>();
        for(IArgument.IChoice arg : args) {
            String key = arg.key();
            String value = arg.value().isBlank() ? key : arg.value();

            if(optionType == OptionType.STRING) {
                choices.add(new Command.Choice(key, value));
            } else if(optionType == OptionType.INTEGER) {
                choices.add(new Command.Choice(key, Integer.parseInt(value)));
            } else if(optionType == OptionType.NUMBER) {
                choices.add(new Command.Choice(key, Double.parseDouble(value)));
            } else {
                throw new RuntimeException("OptionType not supported");
            }
        }

        return choices;
    }

    /**
     * This function is used to extract all the classes in the package.
     * @param pkgName
     * @return All the classes in the package
     */
    public static Set<Class> getClassesInPackage(String pkgName) {
        LOGGER.debug("Searching for classes in package '" + pkgName + "'");
        Reflections reflections = new Reflections(pkgName, Scanners.SubTypes.filterResultsBy(s -> true));
        return new HashSet<>(reflections.getSubTypesOf(Object.class));
    }

    private static <T extends Annotation> String getID(T annotation, String defaultID) {
        if(annotation instanceof IButton) {
            String id = ((IButton) annotation).id();
            return id.isBlank() ? defaultID : id;
        }
        else if(annotation instanceof IMenu) {
            String id = ((IMenu) annotation).id();
            return id.isBlank() ? defaultID : id;
        }
        else if(annotation instanceof IModal) {
            String id = ((IModal) annotation).id();
            return id.isBlank() ? defaultID : id;
        } else {
            throw LOGGER.throwing(new RuntimeException("Annotation not supported"));
        }
    }

    /**
     * This function is used to extract methods annotated by a specific annotation.
     * @param cls The class to search in
     * @param annotationClass The annotation class
     * @param mapper The map to store the methods in
     * @param <T> The annotation class
     */
    public static <T extends Annotation> void getMethodsAnnotatedBy(Class cls, Class<T> annotationClass, Map<String, Method> mapper) {
        getMethodsAnnotatedBy(cls, annotationClass, mapper, null);
    }

    /**
     * This function is used to extract methods annotated by a specific annotation.
     * @param cls The class to search in
     * @param annotationClass The annotation class
     * @param mapper The map to store the methods in
     * @param callback The callback to call when a method is found, it's not necessary to pass the callback if you don't want to
     * @param <T> The annotation class
     */
    public static <T extends Annotation> void getMethodsAnnotatedBy(Class cls, Class<T> annotationClass, Map<String, Method> mapper, AnnotatedByCallback<T> callback) {
        Arrays.stream(cls.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(annotationClass))
            .forEach(method -> {
                T annotation = method.getAnnotation(annotationClass);
                String id = getID(annotation, method.getName());
                if(mapper.containsKey(id)) {
                    throw LOGGER.throwing(new RuntimeException(annotationClass.getSimpleName() + " with id " + id + " already exists"));
                }

                mapper.put(id, method);
                if(callback != null) callback.operation(annotation, method);
                LOGGER.debug("Found " + annotationClass.getSimpleName() + ": " + id);
            });
    }

    /**
     * This function is used to parse an Argument into an OptionData.
     * @param argument The annotation
     * @param methodName The method name
     * @param callback The callback to pass choices, it's not necessary to pass the callback if you don't want to
     * @return The Argument parsed to OptionData
     */
    public static OptionData parserArgument(IArgument argument, String methodName, ParserCallback callback) {
        String     name            = argument.name();
        String     description     = argument.description();
        boolean    required        = argument.required();
        OptionType type            = argument.type();
        boolean    hasAutoComplete = argument.hasAutoComplete();

        OptionData optionData = new OptionData(type, name, description, required, hasAutoComplete);

        if(!hasAutoComplete) {
            optionData.addChoices(Utils.getChoices(argument.choices(), type));
        } else if(callback != null) {
            String key = methodName + ":" +  name;
            callback.operation(key, Utils.getChoices(argument.choices(), argument.type()));
        }
        return optionData;
    }

    /**
     * This function is used to parse a Range of Argument into a list of OptionData
     * @param range
     * @param methodName
     * @param callback
     * @return
     */
    public static List<OptionData> parserRange(IRange range, String methodName, ParserCallback callback) {
        IArgument IArgument = range.value();
        int min = range.min();
        int max = range.max();
        int step = range.steps();

        ArrayList<OptionData> options = new ArrayList<>();

        String description = IArgument.description();
        boolean required = IArgument.required();
        OptionType type = IArgument.type();
        boolean hasAutoComplete = IArgument.hasAutoComplete();
        List<Command.Choice> choices = Utils.getChoices(IArgument.choices(), type);

        if(step <= 0) throw LOGGER.throwing(new RuntimeException("Steps must be greater than 0"));

        for(int value = min; value <= max; value += step) {
            String name = IArgument.name() + value;
            OptionData optionData = new OptionData(type, name, description, required, hasAutoComplete);

            if (!hasAutoComplete) {
                optionData.addChoices(choices);
            } else if(callback != null) {
                String key = methodName + ":" +  name;
                callback.operation(key, choices);
            }
            options.add(optionData);
        }

        return options;
    }
}

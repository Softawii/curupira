package com.softawii.curupira.v2.utils;

import com.softawii.curupira.v2.annotations.DiscordController;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanUtils.class);

    public static Set<Class> getClassesInPackage(String pkgName) {
        LOGGER.info("Searching for classes in package '{}'", pkgName);
        Reflections reflections = new Reflections(pkgName);
        return new HashSet<>(reflections.getSubTypesOf(Object.class));
    }

    public static List<Method> getMethodsAnnotatedWith(Class clazz, Class filtering) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(filtering)).toList();
    }
}

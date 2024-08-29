package com.softawii.curupira.v2.utils;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ScanUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanUtils.class);

    public static Set<Class<?>> getClassesInPackage(String pkg, Class annotation) {
        LOGGER.info("Scanning package: {}, annotation: {}", pkg, annotation);
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(pkg))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));
        return reflections.getTypesAnnotatedWith(annotation);
    }

    public static List<Method> getMethodsAnnotatedWith(Class clazz, Class filtering) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(filtering)).toList();
    }
}

package com.softawii.curupira.v2.utils;

import com.softawii.curupira.v2.annotations.DiscordController;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanUtils.class);

    public static Set<Class<?>> getClassesInPackage(String pkg, Class annotation) {
        LOGGER.info("Scanning package: {}, annotation: {}", pkg, annotation);
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(pkg)
                        .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated)
                        .filterInputsBy((input) -> input.endsWith(".class") && input.startsWith(pkg.replace('.', '/')))
        );
        HashSet<Class<?>> set = new HashSet<Class<?>>(reflections.getTypesAnnotatedWith(annotation));

        for(Class<?> clazz : set.stream().toList()) {
            if(clazz.getPackage().getName().startsWith(pkg)) {
                LOGGER.info("Found class: {}", clazz.getName());
            } else {
                set.remove(clazz);
            }
        }

        return set;
    }

    public static List<Method> getMethodsAnnotatedWith(Class clazz, Class filtering) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(filtering)).toList();
    }
}
